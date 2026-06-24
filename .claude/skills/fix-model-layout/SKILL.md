---
name: fix-model-layout
description: Use while modeling a BPMN process to fix the layout problems that the BPMN linter or /verify-model-visually report — crossing flows, overlaps, flows routed through shapes, invisible or messy elements. Re-routes the affected edges deterministically, edits the diagram coordinates, or regenerates the whole layout — touching only the diagram, never the executable process. The fixing counterpart to verify-model-visually. Trigger phrases: "fix the layout", "fix the crossing flow", "repair the diagram", "clean up the BPMN layout", "auto-fix the model".
allowed-tools: Read, Bash, Edit, Glob, AskUserQuestion
---

# Skill: fix-model-layout

The **fixing** counterpart to `/verify-model-visually`. That skill (and `npm --prefix tools run lint:bpmn`)
_locate_ layout problems; this skill _resolves_ them. Always start from their output — never
guess what is wrong.

There are three ways to fix, in escalating order. Prefer the earliest that applies.

## Key Rules

- **Only ever change DI coordinates** (`bpmndi:` — shape `dc:Bounds`, edge `di:waypoint`, label
  bounds). Never touch `bpmn:` semantics (flows, elements, types). Because layout bugs live
  entirely in the DI layer, a DI-only fix **cannot change what the process does**.
- **Start from the findings.** Run the validators first and fix exactly what they flag.
- **Prefer the deterministic fixer**, then your own DI edit, then full re-layout. Escalate only
  when the cheaper tier can't.
- **Tier 3 needs explicit user approval.** Full auto-layout is destructive — it discards all
  hand-tuned positioning — so never run it unprompted. Always confirm with `AskUserQuestion` first.
- **Re-validate after every fix** — `npm --prefix tools run lint:bpmn` _and_ re-render — until clean. A fix
  that introduces a new finding is not a fix.

## Instructions

1. **Get the findings (the work list).** Often you already have them — if `npm --prefix tools run lint:bpmn`
   or `/verify-model-visually` ran just before in this session, or the user handed you their
   output, **use that list directly; don't re-run to regenerate it.** Only if you have no current
   findings, produce them now: run `npm --prefix tools run lint:bpmn` (structural + geometric) and, for
   aesthetic issues, `/verify-model-visually <file>`. Either way, each finding names the
   element/flow and the rule — that is what you fix. (You _will_ re-run the validators at the end
   to verify — step 5.)

2. **Tier 1 — deterministic reroute** (for edge-routing: `local/flow-through-element`,
   `local/no-crossing-flows`). Run the surgical fixer; it re-routes only the offending flows
   with bpmn.io's ManhattanLayout, preserving everything else:

   ```bash
   npm --prefix tools run fix:bpmn -- <file.bpmn> --write
   ```

   Re-lint. Whatever it reports as **escalated** (it couldn't clear the obstacle), take to Tier 2.

3. **Tier 2 — fix the DI yourself** — for everything Tier 1 can't _fix_. Note that most of
   these are still **deterministically detected**; Tier 2 is about the repair, not the
   detection:
   - **overlapping shapes** — detected by the standard `no-overlapping-elements` rule; the
     linter already names the two shapes. You just nudge one (with its edges + label).
   - **a missing shape** (invisible element) — detected by the standard `no-bpmndi` rule; you
     add the shape on the path between its neighbours.
   - **an escalated edge** — the custom `local/*` rule located it; Tier 1 just couldn't clear it.
   - **only here is judgment the _detector_:** the subjective residue from
     `/verify-model-visually` (communicates intent, semantic grouping, cramped-but-valid, label
     quality) — no rule flags these; the rendered image is your input.

   `Read` the rendered image, then `Edit` the `.bpmn` DI coordinates: move whole groups
   consistently — the shape **and** its edge waypoints **and** its label move together. Re-render
   (`/verify-model-visually` or `npx bpmn-to-image`) and re-lint after each edit.

4. **Tier 3 — full auto-layout** (escape hatch — only if the model has no/garbage DI or is
   beyond surgical repair). This **discards all hand-tuned positioning**, so it is **destructive
   and must not run without explicit user approval**. First use **`AskUserQuestion`** to ask
   whether to regenerate the whole layout — making clear it throws away the hand-tuned positioning
   (happy-path prominence, semantic grouping, boundary-event placement) and that `bpmn-auto-layout`
   does not lay out associations / groups / message flows. Only if the user approves:

   ```bash
   npm --prefix tools run auto-layout:bpmn -- <file.bpmn> --write
   ```

   Then re-lint — the dropped associations / groups / message flows will trip `no-bpmndi` and need
   a Tier-2 touch-up. If the user declines, stay at Tier 2.

5. **Verify.** Confirm all three:
   - **Re-render the image and look at it.** Regenerate the diagram from the edited `.bpmn` —
     `/verify-model-visually <file>`, or `npx bpmn-to-image <file>:.context/verify/<name>.png`
     then `Read` the PNG — and check the picture actually looks right (don't trust the lint exit
     code alone for a _layout_ fix).
   - **Re-lint** — `npm --prefix tools run lint:bpmn` reports zero problems (a fix that adds a new finding
     is not a fix).
   - **Process unchanged** — since you only changed DI, the executable process is untouched; run
     the process integration test if one exists (it only re-confirms the model still parses).

6. **Report as a table** — one row per finding:

   | Finding                      | Element / flow | Fix applied                              | Tier              |
   | ---------------------------- | -------------- | ---------------------------------------- | ----------------- |
   | `local/flow-through-element` | `No` flow      | rerouted around `Send Confirmation Mail` | 1 (deterministic) |

## Choosing a tier

Quick reference once you know the tiers above — which tier fits which problem:

| Problem                                             | Detected by                                                     | Fix tier                                     |
| --------------------------------------------------- | --------------------------------------------------------------- | -------------------------------------------- |
| flow routed through a shape / crossing flows        | custom `local/flow-through-element` / `local/no-crossing-flows` | **1** (escalate to 2 if it can't clear it)   |
| overlapping shapes                                  | **standard** `no-overlapping-elements`                          | **2** (nudge one shape + its edges/label)    |
| missing shape (invisible element)                   | **standard** `no-bpmndi`                                        | **2** (place it; or **3** if no DI at all)   |
| "valid but reads badly" — intent, grouping, cramped | **judgment** (`/verify-model-visually`)                         | **2**                                        |
| model has no diagram interchange at all             | —                                                               | **3** (destructive — needs approval, step 4) |

Only the judgment row needs a human/AI to _find_ the problem; everything else is flagged
deterministically (standard or custom rule). Tier 2 ≠ "needs a human to find it" — it mostly
means "needs a fix the edge-router can't do."

If anything is unclear (which file, which fix to apply), ask before proceeding.
