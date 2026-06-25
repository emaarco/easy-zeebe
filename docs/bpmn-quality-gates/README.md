# Why two BPMN safety nets?

A BPMN model has two layers: the semantic XML (what executes) and the visual DI layer (what
renders). Both humans and AI agents edit the semantic layer well but routinely botch the
visual layer — producing bugs that never show up in a code diff:

- **Invisible elements** — a task that executes but has no shape, so it never renders. The
  process runs; the picture lies.
- **Messy layout** — shapes placed at guessed coordinates that overlap, or sequence flows
  routed so they cross each other or run straight through other elements.

This project guards both with two **complementary** checks that divide the work along one
clean line — _can a coordinate formula decide it?_

> **⚠️ Reference implementation, not a drop-in library.** These quality gates are worked ideas — a starting point to **fork, adapt, and harden** for your own models and conventions. They are **not production-tested**; review what they do and adjust before relying on them.

| Check                                           | What it is                                | Owns                                                                                                                                                        |
| ----------------------------------------------- | ----------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `bpmnlint` (`npm --prefix tools run lint:bpmn`) | fast, deterministic linter                | everything **geometric**: invisible elements, overlapping shapes, crossing flows, flows routed through shapes                                               |
| `/verify-model-visually`                        | multimodal review of the _rendered image_ | everything that needs **judgment**: semantic grouping, whether the layout communicates intent, cramped-but-valid spacing, label quality, intentional idioms |

If a coordinate formula can settle it, it belongs in the cheap deterministic linter. If it
depends on meaning or human perception, it needs the visual review. bpmnlint already ships
overlap (`no-overlapping-elements`) and invisible-element (`no-bpmndi`) checks — but those only
compare **shape bounds**, never **edge geometry**, so crossing flows and flows routed through a
shape slip through. Two small in-repo custom rules — `local/no-crossing-flows` and
`local/flow-through-element` — close that gap. (See
[`probes.md`](./probes.md#custom-layout-rules) and
[`tools/bpmnlint-plugin-local/`](../../tools/bpmnlint-plugin-local).)

## The probes

Four copies of the membership model, each isolating one failure so you can see which net
catches it. Every probe is its own folder under [`probes/`](./probes/) with the model, the
rendered image, and a README showing the **before/after** lint output:

- **[`clean`](./probes/clean/)** — control; everything passes.
- **[`probe-invisible`](./probes/probe-invisible/)** — a task's shape is deleted (it still
  executes, never renders) → bpmnlint `no-bpmndi`.
- **[`probe-messy`](./probes/probe-messy/)** — a flow re-routed through two unrelated elements,
  no shape moved → bpmnlint `local/flow-through-element`. _This used to be the linter's blind
  spot; the custom rule closes it._
- **[`probe-crossing`](./probes/probe-crossing/)** — two independent flows routed to cross →
  bpmnlint `local/no-crossing-flows`.

Every probe is now caught by the **deterministic** net. That is the point: geometry should be
the linter's job. The visual review's remaining, irreducible value is the **subjective**
residue no coordinate rule can settle — see [`probes.md`](./probes.md) for the full results,
the custom rules, and exactly where that boundary falls.

## Fixing what the gates flag

> **⚠️ Reference implementation, not a drop-in library.** The fixers below are worked ideas, **not production-tested** tooling. This applies **especially to the automatic fixers** — they edit diagram geometry heuristically, so review every change they make and harden them before relying on them. Fork and adapt freely.

Detection is only half the loop. The [`/fix-model-layout`](../../.claude/skills/fix-model-layout)
skill resolves a flagged issue three ways, in escalating order — all touching only `bpmndi:`, so
the executable process is provably unchanged:

1. **Deterministic reroute** — `npm --prefix tools run fix:bpmn -- <file> --write` re-routes only the affected
   edges with bpmn.io's `ManhattanLayout` (layout-preserving).
2. **AI edits the DI** — for what the tool escalates, or subjective issues, the agent edits the
   coordinates guided by the rendered image.
3. **Full auto-layout** — `npm --prefix tools run auto-layout:bpmn -- <file> --write` regenerates everything with
   `bpmn-auto-layout` (escape hatch; discards hand-tuned positioning).

The tools live in [`tools/bpmn-fix/`](../../tools/bpmn-fix); worked **before/after** examples of
each variant are in [`fixes/`](./fixes/).
