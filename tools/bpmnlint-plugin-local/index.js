'use strict';

// In-repo bpmnlint plugin: deterministic BPMN layout rules computed from the
// diagram interchange (shape bounds + edge waypoints). These close the gap left
// by core's no-overlapping-elements, which only compares shape-vs-shape bounds.
//
// Wired into tools/.bpmnlintrc as a `file:` devDependency, so it runs under the
// same `npm --prefix tools run lint:bpmn` gate — no npm publishing required.
module.exports = {
  rules: {
    'no-crossing-flows': './rules/no-crossing-flows',
    'flow-through-element': './rules/flow-through-element',
  },
};
