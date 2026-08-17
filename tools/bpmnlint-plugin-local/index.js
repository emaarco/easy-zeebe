'use strict';

// In-repo bpmnlint plugin. Two rule families, both wired into tools/.bpmnlintrc as a
// `file:` devDependency so they run under the same `npm --prefix tools run lint:bpmn` gate —
// no npm publishing required.
//
// - Geometry rules computed from the diagram interchange (shape bounds + edge waypoints),
//   closing the gap left by core's no-overlapping-elements (shape-vs-shape bounds only).
// - Styleguide rules enforcing the ID/name conventions from docs/bpmn-styleguide/styleguide.md
//   (element ids, message names, Zeebe task types).
module.exports = {
  rules: {
    'no-crossing-flows': './rules/no-crossing-flows',
    'flow-through-element': './rules/flow-through-element',
    'element-id': './rules/element-id',
    'message-id': './rules/message-id',
    'task-type': './rules/task-type',
  },
};
