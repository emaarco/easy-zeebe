---
name: fix-model-layout
description: Use while modeling a BPMN process to fix the layout problems that the BPMN linter or /verify-model-visually report — crossing flows, overlaps, flows routed through shapes, invisible or messy elements. Edits the diagram coordinates or regenerates the whole layout — touching only the diagram, never the executable process. The fixing counterpart to verify-model-visually. Trigger phrases: "fix the layout", "fix the crossing flow", "repair the diagram", "clean up the BPMN layout", "auto-fix the model".
allowed-tools: Read, Bash, Edit, Glob, AskUserQuestion
---

# Skill: fix-model-layout

The **fixing** counterpart to `/verify-model-visually`. That skill (and `npm --prefix tools run lint:bpmn`)
_locate_ layout problems; this skill _resolves_ them. Always start from their output — never
guess what is wrong.

There are two ways to fix, in escalating order. Prefer the earlier that applies.

## Key Rules

- **Only ever change DI coordinates** (`bpmndi:` — shape `dc:Bounds`, edge `di:waypoint`, label
  bounds). Never touch `bpmn:` semantics (flows, elements, types). Because layout bugs live
  entirely in the DI layer, a DI-only fix **cannot change what the process does**.
- **Start from the findings.** Run the validators first and fix exactly what they flag.
- **Prefer a targeted DI edit**, then full re-layout. Escalate only when a hand edit can't.
- **Full auto-layout needs explicit user approval.** It is destructive — it discards all
  hand-tuned positioning — so never run it unprompted. Always confirm with `AskUserQuestion` first.
- **Re-validate after every fix** — `npm --prefix tools run lint:bpmn` _and_ re-render — until clean. A fix
  that introduces a new finding is not a fix. (Note the geometry findings from
  `@miragon/rules/*` are **warnings**, not errors — clear them anyway; they mark real layout bugs.)

## Instructions

1. **Get the findings (the work list).** Often you already have them — if `npm --prefix tools run lint:bpmn`
   or `/verify-model-visually` ran just before in this session, or the user handed you their
   output, **use that list directly; don't re-run to regenerate it.** Only if you have no current
   findings, produce them now: run `npm --prefix tools run lint:bpmn` (structural + geometric) and, for
   aesthetic issues, `/verify-model-visually <file>`. Either way, each finding names the
   element/flow and the rule — that is what you fix. (You _will_ re-run the validators at the end
   to verify — step 4.)

2. **Tier 1 — fix the DI yourself.** Most layout problems are **deterministically detected**;
   Tier 1 is about the repair. The detector names the element for you:
   - **crossing flows / a flow routed through a shape** — detected by `@miragon/rules/flow-crossing`
     and `@miragon/rules/flow-through-element`; re-route the offending edge by editing its
     `di:waypoint`s so it runs orthogonally around the obstacle.
   - **overlapping shapes** — detected by the standard `no-overlapping-elements` rule; the
     linter already names the two shapes. Nudge one (with its edges + label).
   - **a missing shape** (invisible element) — detected by the standard `no-bpmndi` rule; you
     add the shape on the path between its neighbours.
   - **only here is judgment the _detector_:** the subjective residue from
     `/verify-model-visually` (communicates intent, semantic grouping, cramped-but-valid, label
     quality) — no rule flags these; the rendered image is your input.

   `Read` the rendered image, then `Edit` the `.bpmn` DI coordinates: move whole groups
   consistently — the shape **and** its edge waypoints **and** its label move together. Re-render
   (`/verify-model-visually` or `npx bpmn-to-image`) and re-lint after each edit.

3. **Tier 2 — full auto-layout** (escape hatch — only if the model has no/garbage DI or is
   beyond hand repair). This **discards all hand-tuned positioning**, so it is **destructive
   and must not run without explicit user approval**. First use **`AskUserQuestion`** to ask
   whether to regenerate the whole layout — making clear it throws away the hand-tuned positioning
   (happy-path prominence, semantic grouping, boundary-event placement) and that `bpmn-auto-layout`
   does not lay out associations / groups / message flows. Only if the user approves:

   ```bash
   npm --prefix tools run auto-layout:bpmn -- <file.bpmn> --write
   ```

   Then re-lint — the dropped associations / groups / message flows will trip `no-bpmndi` and need
   a Tier-1 touch-up. If the user declines, stay at Tier 1.

4. **Verify.** Confirm all three:
   - **Re-render the image and look at it.** Regenerate the diagram from the edited `.bpmn` —
     `/verify-model-visually <file>`, or `npx bpmn-to-image <file>:.context/verify/<name>.png`
     then `Read` the PNG — and check the picture actually looks right (don't trust the lint exit
     code alone for a _layout_ fix).
   - **Re-lint** — `npm --prefix tools run lint:bpmn` reports no `error`-level problems and the geometry
     warnings you set out to fix are gone (a fix that adds a new finding is not a fix).
   - **Process unchanged** — since you only changed DI, the executable process is untouched; run
     the process integration test if one exists (it only re-confirms the model still parses).

5. **Report as a table** — one row per finding:

   | Finding                               | Element / flow | Fix applied                              | Tier             |
   | ------------------------------------- | -------------- | ---------------------------------------- | ---------------- |
   | `@miragon/rules/flow-through-element` | `No` flow      | rerouted around `Send Confirmation Mail` | 1 (hand DI edit) |

## Choosing a tier

Quick reference once you know the tiers above — which tier fits which problem:

| Problem                                             | Detected by                                                            | Fix tier                                     |
| --------------------------------------------------- | ---------------------------------------------------------------------- | -------------------------------------------- |
| flow routed through a shape / crossing flows        | `@miragon/rules/flow-through-element` / `@miragon/rules/flow-crossing` | **1** (re-route the edge's waypoints)        |
| overlapping shapes                                  | **standard** `no-overlapping-elements`                                 | **1** (nudge one shape + its edges/label)    |
| missing shape (invisible element)                   | **standard** `no-bpmndi`                                               | **1** (place it; or **2** if no DI at all)   |
| "valid but reads badly" — intent, grouping, cramped | **judgment** (`/verify-model-visually`)                                | **1**                                        |
| model has no diagram interchange at all             | —                                                                      | **2** (destructive — needs approval, step 3) |

Only the judgment row needs a human/AI to _find_ the problem; everything else is flagged
deterministically (standard or Miragon rule).

If anything is unclear (which file, which fix to apply), ask before proceeding.
