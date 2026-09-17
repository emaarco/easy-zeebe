'use strict';

// In-repo bpmnlint plugin, wired into tools/.bpmnlintrc as a `file:` devDependency so it runs
// under the same `npm --prefix tools run lint:bpmn` gate — no npm publishing required.
//
// Holds only the styleguide rules that have no equivalent in @miragon/bpmnlint-plugin-rules:
// the message-name and Zeebe task-type conventions from docs/bpmn-styleguide/styleguide.md.
// Geometry (crossing flows, flow-through-element) and element-id naming are now covered by the
// Miragon package.
module.exports = {
  rules: {
    'message-id': './rules/message-id',
    'task-type': './rules/task-type',
  },
};
