'use strict';

// Shared geometry helpers for the local layout rules. Everything is computed
// from the BPMNDI: shape bounds (dc:Bounds) and edge waypoints — the same data
// the diagram is rendered from. No semantic guessing, just coordinates.

// inheritance-safe moddle type test (handles bpmn:Transaction < bpmn:SubProcess etc.);
// defensive against malformed DI where bpmnElement may not be a moddle object.
const inst = (el, type) => !!el && typeof el.$instanceOf === 'function' && el.$instanceOf(type);

// Shapes that ENCLOSE flows, so a flow drawn "over" them is not a defect:
// pools/lanes/groups always, and an EXPANDED sub-process (a collapsed one is a
// task-sized box and IS a real obstacle).
function isEnclosingContainer(el, isExpanded) {
  if (inst(el, 'bpmn:Participant') || inst(el, 'bpmn:Lane') || inst(el, 'bpmn:Group')) return true;
  if (inst(el, 'bpmn:SubProcess')) return isExpanded === true;
  return false;
}

// Shapes a sequence flow may legitimately pass over (never a routing obstacle):
// enclosing containers, boundary events (border noise), and decorative overlays —
// artifacts (text annotations, groups) and data objects/stores/inputs/outputs.
function isPassableShape(el, isExpanded) {
  return (
    isEnclosingContainer(el, isExpanded) ||
    inst(el, 'bpmn:BoundaryEvent') ||
    inst(el, 'bpmn:Artifact') ||
    inst(el, 'bpmn:ItemAwareElement')
  );
}

// Collect DI grouped BY PLANE. Each BPMNPlane (the root process/collaboration and
// every drill-down of a collapsed sub-process / call activity) is its own coordinate
// space — geometry must never be compared across planes.
function collectByPlane(definitions) {
  const planes = [];
  for (const diagram of definitions.diagrams || []) {
    const plane = diagram.plane;
    if (!plane) continue;
    const shapes = [];
    const edges = [];
    for (const pe of plane.planeElement || []) {
      const el = pe.bpmnElement;
      if (!el) continue;
      if (pe.$type === 'bpmndi:BPMNShape' && pe.bounds) {
        shapes.push({ el, bounds: pe.bounds, isExpanded: pe.isExpanded === true });
      } else if (pe.$type === 'bpmndi:BPMNEdge' && pe.waypoint && pe.waypoint.length > 1) {
        edges.push({ el, waypoints: pe.waypoint });
      }
    }
    planes.push({ shapes, edges });
  }
  return planes;
}

const orient = (a, b, c) => Math.sign((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x));

// Proper crossing: each segment strictly separates the other's endpoints.
// Excludes collinear overlap and shared/touching endpoints, so flows that merge
// or branch at a common node (and therefore meet there) are NOT reported.
function properCross(p1, p2, p3, p4) {
  const o1 = orient(p1, p2, p3);
  const o2 = orient(p1, p2, p4);
  const o3 = orient(p3, p4, p1);
  const o4 = orient(p3, p4, p2);
  return o1 * o2 < 0 && o3 * o4 < 0;
}

// Consecutive waypoint pairs as line segments.
const segments = (wps) => wps.slice(0, -1).map((p, i) => [p, wps[i + 1]]);

const inside = (p, r, pad = 1) =>
  p.x > r.x + pad && p.x < r.x + r.width - pad && p.y > r.y + pad && p.y < r.y + r.height - pad;

// Does a segment enter the rectangle's interior (not merely graze a corner/border)?
function segThroughRect(a, b, r) {
  if (inside(a, r) || inside(b, r)) return true;
  const c = [
    { x: r.x, y: r.y },
    { x: r.x + r.width, y: r.y },
    { x: r.x + r.width, y: r.y + r.height },
    { x: r.x, y: r.y + r.height },
  ];
  for (let i = 0; i < 4; i++) {
    if (properCross(a, b, c[i], c[(i + 1) % 4])) return true;
  }
  return false;
}

// node ids a sequence flow connects (its source + target).
const flowEndpointIds = (flow) =>
  [flow.sourceRef && flow.sourceRef.id, flow.targetRef && flow.targetRef.id].filter(Boolean);

// Axis-aligned bounding boxes for a cheap disjoint-reject before exact geometry.
const bboxOfPoints = (pts) =>
  pts.reduce(
    (b, p) => ({
      minX: Math.min(b.minX, p.x),
      minY: Math.min(b.minY, p.y),
      maxX: Math.max(b.maxX, p.x),
      maxY: Math.max(b.maxY, p.y),
    }),
    { minX: Infinity, minY: Infinity, maxX: -Infinity, maxY: -Infinity }
  );
const bboxOfRect = (r) => ({ minX: r.x, minY: r.y, maxX: r.x + r.width, maxY: r.y + r.height });
const bboxDisjoint = (a, b) => a.maxX < b.minX || b.maxX < a.minX || a.maxY < b.minY || b.maxY < a.minY;

module.exports = {
  inst,
  isPassableShape,
  collectByPlane,
  properCross,
  segments,
  segThroughRect,
  flowEndpointIds,
  bboxOfPoints,
  bboxOfRect,
  bboxDisjoint,
};
