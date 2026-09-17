# Fixing — worked examples

> **⚠️ Reference implementation, not a drop-in library.** The fixers below are worked ideas, **not production-tested** tooling. This applies **especially to the automatic fixers** — they edit diagram geometry heuristically, so review every change they make and harden them before relying on them. Fork and adapt freely.

The [`probes/`](../probes/) prove **detection**; these prove **repair**. Each folder is a
self-contained before/after demo of one of the two fix variants from the
[`/fix-model-layout`](../../../.claude/skills/fix-model-layout) skill, run on a broken model —
with the real lint output and rendered images. Both touch **only `bpmndi:`**, so the
executable process is unchanged.

| Folder                           | Variant                                                                             | Demonstrates                                                                                                                      |
| -------------------------------- | ----------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| [`ai-edit/`](./ai-edit/)         | **AI edits the DI**                                                                 | an **overlap** (standard `no-overlapping-elements`) the agent nudges the shape clear of → **0 problems**                          |
| [`auto-layout/`](./auto-layout/) | **Full auto-layout** — `npm --prefix tools run auto-layout:bpmn` (bpmn-auto-layout) | `probe-messy` regenerated from scratch — clean but **generic**, discards the layout **and** drops the association (1 `no-bpmndi`) |

The escalation order is deliberate: the hand DI-edit preserves the most and risks the least;
full auto-layout is the last resort. See the
[`/fix-model-layout`](../../../.claude/skills/fix-model-layout/SKILL.md) skill for which variant
fits which problem.
