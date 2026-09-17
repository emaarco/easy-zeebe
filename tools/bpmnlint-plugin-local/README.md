# bpmnlint-plugin-local

In-repo [bpmnlint](https://github.com/bpmn-io/bpmnlint) plugin holding the **styleguide** rules
that have no equivalent in the shared [`@miragon/bpmnlint-plugin-rules`](https://github.com/Miragon/bpmnlint-rules)
package: the ID and name conventions from
[`docs/bpmn-styleguide/styleguide.md`](../../docs/bpmn-styleguide/styleguide.md), so a mismatched
message name or Zeebe task type is caught before it reaches `bpmn-to-code` or the engine.

Geometry (crossing flows, flows routed through a shape) and element-id naming used to live here
too; they are now covered by `@miragon/bpmnlint-plugin-rules` (`@miragon/rules/flow-crossing`,
`@miragon/rules/flow-through-element`, `@miragon/rules/element-id-naming`) and were removed.

Part of the project's BPMN quality gates — see
[`docs/bpmn-quality-gates/`](../../docs/bpmn-quality-gates/) for the full story and the probes.

> **⚠️ Reference implementation, not a drop-in library.** These quality gates are worked ideas — a starting point to **fork, adapt, and harden** for your own models and conventions. They are **not production-tested**; review what they do and adjust before relying on them.

## The rules

| Rule               | Default | Detects                                                                                                                                              |
| ------------------ | ------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| `local/message-id` | error   | a `bpmn:Message` **name** (the correlation key, not the `Message_<hash>` id) that does not read `<serviceName>.<state>`, both camelCase              |
| `local/task-type`  | error   | a service task's Zeebe **task type** (`zeebe:taskDefinition type=…`) that does not read `<serviceName>.<elementIdWithoutTypePrefix>`, both camelCase |

## How it is wired (no npm publish)

This package is **not** published. It is consumed locally:

1. `tools/package.json` references it as a `file:` devDependency:
   ```json
   "bpmnlint-plugin-local": "file:bpmnlint-plugin-local"
   ```
2. `tools/.bpmnlintrc` turns the rules on under the `local/` prefix:
   ```json
   "rules": {
     "local/message-id": "error",
     "local/task-type": "error"
   }
   ```

So they run under the same `npm --prefix tools run lint:bpmn` gate as every other rule —
`npm --prefix tools install` (or `npm --prefix tools ci` in CI) links the plugin into
`node_modules`, and bpmnlint resolves `local/<rule>` to `bpmnlint-plugin-local/rules/<rule>`.

## Files

```
index.js                      rule registry (maps local/<name> -> ./rules/<name>)
rules/message-id.js           local/message-id           (styleguide)
rules/task-type.js            local/task-type            (styleguide)
```
