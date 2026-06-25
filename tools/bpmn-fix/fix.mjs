#!/usr/bin/env node
// Deterministic, layout-preserving BPMN fixer (Tier 1).
//
// Reuses the SAME geometry the lint rules use to LOCATE the broken edges
// (flows routed through a shape, crossing flows), then recomputes clean
// orthogonal waypoints for just those edges with bpmn.io's own
// diagram-js ManhattanLayout — touching ONLY BPMNDI waypoints, never bpmn:
// semantics, so the executable process is provably unchanged.
//
// Usage:  npm --prefix tools run fix:bpmn -- <file.bpmn> [--write]
//   (default: dry-run report; --write applies the fix in place)
//   <file.bpmn> is resolved from the directory you ran npm in (repo root).
import { BpmnModdle } from 'bpmn-moddle';
import { readFileSync, writeFileSync } from 'node:fs';
import { createRequire } from 'node:module';
import { resolve } from 'node:path';

const require = createRequire(import.meta.url);
const { connectRectangles, withoutRedundantPoints } = require('diagram-js/lib/layout/ManhattanLayout');
const { segThroughRect, isPassableShape, segments, properCross, flowEndpointIds } = require(
  '../bpmnlint-plugin-local/rules/_geometry.js'
);

const [arg, ...flags] = process.argv.slice(2);
const write = flags.includes('--write');
if (!arg) {
  console.error('usage: npm --prefix tools run fix:bpmn -- <file.bpmn> [--write]');
  process.exit(2);
}
// npm runs this script with cwd=tools/; resolve the file against the dir the
// user invoked npm in (INIT_CWD = repo root) so paths stay repo-root-relative.
const file = resolve(process.env.INIT_CWD || process.cwd(), arg);

const moddle = new BpmnModdle();
const { rootElement: defs } = await moddle.fromXML(readFileSync(file, 'utf8'));
const rect = (b) => ({ x: b.x, y: b.y, width: b.width, height: b.height });

let rerouted = 0;
let escalated = 0;

for (const dia of defs.diagrams || []) {
  if (!dia.plane) continue;
  const shapes = [];
  const edges = [];
  for (const pe of dia.plane.planeElement || []) {
    if (!pe.bpmnElement) continue;
    if (pe.$type === 'bpmndi:BPMNShape' && pe.bounds) shapes.push({ pe, el: pe.bpmnElement, bounds: pe.bounds, isExpanded: pe.isExpanded === true });
    else if (pe.$type === 'bpmndi:BPMNEdge' && pe.waypoint?.length > 1) edges.push({ pe, el: pe.bpmnElement, get waypoints() { return this.pe.waypoint; } });
  }
  const boundsOf = new Map(shapes.map((s) => [s.el.id, s.bounds]));
  const flows = edges.filter((e) => e.el.$type === 'bpmn:SequenceFlow');

  // obstacles a given flow may not pass through (mirror flow-through-element)
  const obstaclesFor = (flow) => {
    const ends = new Set(flowEndpointIds(flow.el));
    for (const r of [flow.el.sourceRef, flow.el.targetRef])
      if (r?.$instanceOf?.('bpmn:BoundaryEvent') && r.attachedToRef) ends.add(r.attachedToRef.id);
    return shapes.filter((s) => !isPassableShape(s.el, s.isExpanded) && !ends.has(s.el.id));
  };
  const throughAny = (wps, obstacles) =>
    segments(wps).some(([a, b]) => obstacles.some((o) => segThroughRect(a, b, o.bounds)));
  const crossesAny = (flow, others) =>
    others.some((o) => {
      if (o === flow) return false;
      if (flowEndpointIds(flow.el).some((id) => flowEndpointIds(o.el).includes(id))) return false;
      return segments(flow.waypoints).some(([a, b]) => segments(o.waypoints).some(([c, d]) => properCross(a, b, c, d)));
    });

  // problem flows: routed through a shape, or crossing another flow
  const problems = flows.filter((f) => throughAny(f.waypoints, obstaclesFor(f)) || crossesAny(f, flows));

  for (const flow of problems) {
    const src = boundsOf.get(flow.el.sourceRef?.id);
    const tgt = boundsOf.get(flow.el.targetRef?.id);
    if (!src || !tgt) { escalated++; console.log(`  ⚠️  ${flow.el.id}: source/target shape missing — cannot reroute`); continue; }

    const wps = flow.waypoints;
    const fmt = (pts) => pts.map((p) => `(${Math.round(p.x)},${Math.round(p.y)})`).join(' ');
    const before = fmt(wps);
    // bpmn.io ManhattanLayout: clean orthogonal route between the two boxes,
    // docking from the current end anchors.
    const routed = withoutRedundantPoints(
      connectRectangles(rect(src), rect(tgt), wps[0], wps[wps.length - 1])
    );

    // no-op: this flow's route is already clean — it was only flagged because a
    // crossing partner (now rerouted) intersected it. Don't rewrite or count it.
    if (fmt(routed) === before) continue;

    // verify the recomputed route actually clears the obstacles (ManhattanLayout
    // routes directly, it does not avoid obstacles — so we must check).
    if (throughAny(routed, obstaclesFor(flow))) {
      escalated++;
      console.log(`  ⚠️  ${flow.el.id}: ManhattanLayout reroute still hits a shape — escalate (region re-layout / AI / auto-layout)`);
      continue;
    }
    flow.pe.waypoint = routed.map((p) => moddle.create('dc:Point', { x: Math.round(p.x), y: Math.round(p.y) }));
    rerouted++;
    console.log(`  ✏️  rerouted ${flow.el.id}\n        before: ${before}\n        after:  ${fmt(routed)}`);
  }
}

console.log(`\n  ${rerouted} flow(s) rerouted, ${escalated} escalated`);
if (write && rerouted > 0) {
  const { xml } = await moddle.toXML(defs, { format: true });
  writeFileSync(file, xml);
  console.log(`  ✅ written in place: ${file}`);
} else if (rerouted > 0) {
  console.log(`  (dry run — re-run with --write to apply)`);
}
