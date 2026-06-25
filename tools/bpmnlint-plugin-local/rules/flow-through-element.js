'use strict';

const {
  inst,
  collectByPlane,
  segments,
  segThroughRect,
  isPassableShape,
  flowEndpointIds,
  bboxOfPoints,
  bboxOfRect,
  bboxDisjoint,
} = require('./_geometry');

/**
 * Reports a sequence flow whose drawn path is routed THROUGH the body of a
 * shape it does not connect to (e.g. a flow slashing across an unrelated task).
 *
 * This is the gap bpmnlint's no-overlapping-elements leaves open: that rule
 * only compares shape-vs-shape bounds and never inspects edge geometry.
 *
 * Deliberately conservative — comparison is scoped per BPMNPlane, and shapes a
 * flow may legitimately pass over are excluded: enclosing containers (expanded
 * sub-processes, pools, lanes, groups), boundary events, and decorative overlays
 * (text annotations, data objects/stores). A collapsed sub-process is a
 * task-sized box and IS treated as an obstacle.
 *
 * @type { import('bpmnlint').RuleFactory }
 */
module.exports = function () {
  function check(node, reporter) {
    if (node.$type !== 'bpmn:Definitions') return;

    for (const plane of collectByPlane(node)) {
      const obstacles = plane.shapes
        .filter((s) => !isPassableShape(s.el, s.isExpanded))
        .map((s) => ({ ...s, bbox: bboxOfRect(s.bounds) }));
      const flows = plane.edges.filter((e) => e.el.$type === 'bpmn:SequenceFlow');

      for (const flow of flows) {
        // exclude the flow's own source/target, and — when an endpoint is a
        // boundary event — its host activity (the flow starts on the host border).
        const endpoints = new Set(flowEndpointIds(flow.el));
        for (const ref of [flow.el.sourceRef, flow.el.targetRef]) {
          if (inst(ref, 'bpmn:BoundaryEvent') && ref.attachedToRef) {
            endpoints.add(ref.attachedToRef.id);
          }
        }
        const segs = segments(flow.waypoints);
        const fbox = bboxOfPoints(flow.waypoints);

        for (const shape of obstacles) {
          if (endpoints.has(shape.el.id)) continue;
          if (bboxDisjoint(fbox, shape.bbox)) continue; // cheap reject
          if (segs.some(([a, b]) => segThroughRect(a, b, shape.bounds))) {
            reporter.report(flow.el.id, `Sequence flow is routed through element <${shape.el.id}>`);
          }
        }
      }
    }
  }

  return { check };
};
