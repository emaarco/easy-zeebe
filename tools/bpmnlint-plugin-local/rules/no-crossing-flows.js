'use strict';

const {
  collectByPlane,
  properCross,
  segments,
  flowEndpointIds,
  bboxOfPoints,
  bboxDisjoint,
} = require('./_geometry');

/**
 * Reports pairs of sequence flows whose drawn paths cross.
 *
 * Edge crossings are the single strongest predictor of poor diagram
 * readability in the graph-drawing literature. Flows that share a node
 * (merge/branch) are expected to meet and are not reported. Comparison is
 * scoped per BPMNPlane so drill-down diagrams never collide.
 *
 * Known limitation: within ONE collaboration plane, two flows in different
 * pools could in principle cross in the inter-pool gap; that is a rare,
 * warn-level smell left to the visual review. Collinear-overlapping flows are
 * also out of scope by design (the visual review backstops them).
 *
 * @type { import('bpmnlint').RuleFactory }
 */
module.exports = function () {
  function check(node, reporter) {
    if (node.$type !== 'bpmn:Definitions') return;

    for (const plane of collectByPlane(node)) {
      const flows = plane.edges
        .filter((e) => e.el.$type === 'bpmn:SequenceFlow')
        .map((f) => ({ ...f, bbox: bboxOfPoints(f.waypoints) }));

      for (let i = 0; i < flows.length; i++) {
        for (let j = i + 1; j < flows.length; j++) {
          const a = flows[i];
          const b = flows[j];

          if (bboxDisjoint(a.bbox, b.bbox)) continue; // cheap reject
          const aIds = flowEndpointIds(a.el);
          const bIds = flowEndpointIds(b.el);
          if (aIds.some((id) => bIds.includes(id))) continue; // share a node -> meet by design

          const crosses = segments(a.waypoints).some(([p1, p2]) =>
            segments(b.waypoints).some(([p3, p4]) => properCross(p1, p2, p3, p4))
          );

          if (crosses) {
            reporter.report(a.el.id, `Sequence flow crosses sequence flow <${b.el.id}>`);
          }
        }
      }
    }
  }

  return { check };
};
