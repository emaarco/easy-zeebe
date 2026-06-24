# Fixing — worked examples

> **⚠️ Reference implementation, not a drop-in library.** The fixers below are worked ideas, **not production-tested** tooling. This applies **especially to the automatic fixers** — they edit diagram geometry heuristically, so review every change they make and harden them before relying on them. Fork and adapt freely.

The [`probes/`](../probes/) prove **detection**; these prove **repair**. Each folder is a
self-contained before/after demo of one of the three fix variants from the
[`/fix-model-layout`](../../../.claude/skills/fix-model-layout) skill, run on a broken model —
with the real lint output and rendered images. All three touch **only `bpmndi:`**, so the
executable process is unchanged.

| Folder                                               | Variant                                                                   | Demonstrates                                                                                                                      |
| ---------------------------------------------------- | ------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| [`deterministic-reroute/`](./deterministic-reroute/) | **Tier 1** — `npm --prefix tools run fix:bpmn` (ManhattanLayout)          | `probe-messy`'s through-shape flow rerouted, layout preserved → **0 problems**                                                    |
| [`ai-edit/`](./ai-edit/)                             | **Tier 2** — the AI edits the DI                                          | an **overlap** (standard `no-overlapping-elements`) that Tier 1 _can't_ fix → AI nudges the shape → **0 problems**                |
| [`auto-layout/`](./auto-layout/)                     | **Tier 3** — `npm --prefix tools run auto-layout:bpmn` (bpmn-auto-layout) | `probe-messy` regenerated from scratch — clean but **generic**, discards the layout **and** drops the association (1 `no-bpmndi`) |

The escalation order is deliberate: Tier 1 preserves the most and risks the least; Tier 3 is the
last resort. See the [`/fix-model-layout`](../../../.claude/skills/fix-model-layout/SKILL.md)
skill — its _Choosing a tier_ table — for which tier fits which problem.
