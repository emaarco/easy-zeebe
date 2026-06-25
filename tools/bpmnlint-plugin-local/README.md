# bpmnlint-plugin-local

In-repo [bpmnlint](https://github.com/bpmn-io/bpmnlint) plugin with deterministic **BPMN
layout** rules. Part of the project's BPMN quality gates — see
[`docs/bpmn-quality-gates/`](../../docs/bpmn-quality-gates/) for the full story and the probes.

## Why this exists

> **⚠️ Reference implementation, not a drop-in library.** These quality gates are worked ideas — a starting point to **fork, adapt, and harden** for your own models and conventions. They are **not production-tested**; review what they do and adjust before relying on them.

bpmnlint ships exactly two rules that touch the diagram-interchange (DI) layer:

- `no-bpmndi` — a semantic element that has **no shape** at all (the "invisible element" bug), and
- `no-overlapping-elements` — **shape-vs-shape** bounding-box overlap.

Both only ever look at **shape bounds**. Neither inspects **edge waypoints** — so a sequence
flow re-routed _across_ the diagram, crossing another flow or running straight _through_ an
unrelated task, passes the linter untouched. That was a real blind spot (our `probe-messy`
fixture proved it): the flow is messy in the picture, but every shape is still where it
belongs, so no shipped rule fires.

The wider BPMN ecosystem has nothing for this either — every published `bpmnlint-plugin-*` is
semantic / engine-compatibility / vendor-specific; none touch layout. So we wrote the two
missing geometric rules ourselves. They compute purely over the DI (shape `dc:Bounds` + edge
`waypoint`s) — the same data the diagram is rendered from — modelled on core's
`no-overlapping-elements.js`.

## The rules

| Rule                         | Default | Detects                                                                                                    |
| ---------------------------- | ------- | ---------------------------------------------------------------------------------------------------------- |
| `local/flow-through-element` | error   | a sequence flow whose drawn path enters the interior of a shape it does not connect to                     |
| `local/no-crossing-flows`    | error   | two sequence flows whose drawn paths cross (flows that share a node are expected to meet and are excluded) |

## How it is wired (no npm publish)

This package is **not** published. It is consumed locally:

1. `tools/package.json` references it as a `file:` devDependency:
   ```json
   "bpmnlint-plugin-local": "file:tools/bpmnlint-plugin-local"
   ```
2. `tools/.bpmnlintrc` turns the rules on under the `local/` prefix:
   ```json
   "rules": {
     "local/no-crossing-flows": "error",
     "local/flow-through-element": "error"
   }
   ```

So they run under the same `npm --prefix tools run lint:bpmn` gate as every other rule —
`npm --prefix tools install` (or `npm --prefix tools ci` in CI) links the plugin into
`node_modules`, and bpmnlint resolves `local/<rule>` to `bpmnlint-plugin-local/rules/<rule>`.

## Design notes

The rules are deliberately **conservative** — a false positive blocks a valid model and erodes
trust, so the priority is zero false positives (a missed case is backstopped by the
`/verify-model-visually` review). To that end:

- comparison is **scoped per diagram plane**, so a collapsed sub-process / call-activity
  drill-down (its own coordinate space) never collides with the main plane;
- shapes a flow may legitimately pass over are excluded: **expanded** containers (sub-processes,
  pools, lanes, groups), **boundary events** (they sit on an activity border where flows
  converge), and decorative **artifacts** (text annotations, data objects/stores);
- container membership uses moddle **inheritance** (`$instanceOf`), so `bpmn:Transaction` and
  `bpmn:AdHocSubProcess` count as sub-processes; a **collapsed** sub-process is a task-sized box
  and _is_ treated as an obstacle;
- a cheap bounding-box reject runs before the exact geometry test.

These exclusions were hardened by an adversarial review (it found and we fixed the
multi-plane, data-object, text-annotation and `Transaction` cases). **Known non-goals**, left
to the visual review: collinear-overlapping flows, a flow over a text _label_,
message-flow/association routing, and cross-pool crossings within a single collaboration plane.

## Files

```
index.js                      rule registry (maps local/<name> -> ./rules/<name>)
rules/_geometry.js            shared DI extraction + segment/rect geometry
rules/no-crossing-flows.js    local/no-crossing-flows
rules/flow-through-element.js local/flow-through-element
```
