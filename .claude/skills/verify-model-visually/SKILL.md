---
name: verify-model-visually
description: Use while modeling a BPMN process to review a model you just edited. Runs both quality nets — the deterministic bpmnlint geometry linter and a visual review of the rendered image — and reports a combined verdict. Catches invisible elements, overlaps, crossing flows, flows routed through a shape (lint), plus messy or hard-to-read layout that needs judgment (image). Use after editing a model, or when asked to review the diagram, check the layout, or verify the model looks right.
allowed-tools: Read, Bash, Glob
---

# Skill: verify-model-visually

Give the agent the feedback loop it lacks. BPMN bugs at the _visual_ layer (BPMNDI) are
invisible in the XML — a task can execute but never render; a shape can sit at guessed
coordinates that overlap a neighbour or make a flow cross a label — and none of it shows up in a
code diff. A model is guarded by **two complementary nets, and this skill runs both**:

- **bpmnlint** (`npm --prefix tools run lint:bpmn`) — the **deterministic** net: invisible
  elements, overlaps, crossing flows, flows routed through a shape. Fast, exact, no judgment.
- **the rendered image** — the **judgment** net: intent, semantic grouping, cramped-but-valid
  spacing, label quality — what no formula can settle.

Run lint first, then look at the picture. (Lint also runs in the pre-commit hook and CI; running
it here makes this a _complete_ review, not just the image half.) A geometric defect the image
catches that lint missed is itself a finding — call it out.

## Key Rules

- **Run both nets and report both.** Lint for geometry, image for judgment. A green lint with
  an ugly picture still fails the review; a clean picture with a red lint still fails.
- **Review the rendered image, not the XML.** Never reason about layout from `<dc:Bounds>` or
  waypoints. Open the PNG and judge what a human sees.
- **Check the model against the styleguide's Layout Guidelines.** Load
  `docs/bpmn-styleguide/styleguide.md` (the _Layout Guidelines_ section) each run; do not rely
  on memory.
- **Report concrete, _located_ issues.** Name the element or flow and the rule it breaks
  ("the `Send Welcome Mail` task overlaps the sub-process border" / "the `No` flow crosses the
  `Send Confirmation Mail` task"). Never give a vague verdict like "looks a bit off".
- **If everything passes, say so explicitly** and list what each net checked.

## Instructions

1. **Resolve the `.bpmn` file.** Use the path the user gave. If none, `Glob` for `**/*.bpmn`
   (production models live under `services/**/src/main/resources/bpmn/`). If more than one
   matches and the target is ambiguous, ask which file to review before continuing.

2. **Run the deterministic linter first (geometry net).**

   ```bash
   npm --prefix tools run lint:bpmn
   ```

   It globs every production model (yours is in there). Clean = silent, exit 0; non-zero = real
   geometry problems — **capture each finding** (every line names the element/flow + rule) for the
   report, but don't stop; still do the visual pass. Missing binary → `npm --prefix tools install`
   once, then re-run. **Skip this step only when geometry was already gated upstream this run**
   (CI's dedicated lint job, or just after the pre-commit hook); then go straight to the visual
   pass and note lint ran upstream.

3. **Render the model to PNG** with bpmn-to-image
   ([bpmn-io/bpmn-to-image](https://github.com/bpmn-io/bpmn-to-image)) — the same tool the
   `bpmn-export` skill uses, so no new dependency. Write to a scratch path:

   ```bash
   npx bpmn-to-image <path-to-bpmn>:.context/verify/<name>.png
   ```

   If the render command fails, stop and report the error (a model that will not render is
   itself a finding).

4. **Load the inputs.** `Read` the rendered PNG (it loads as an image) and `Read`
   `docs/bpmn-styleguide/styleguide.md` (the project BPMN styleguide — focus on its **Layout
   Guidelines** section; the rest covers naming, IDs, and automation).

   If labels are too small to read — the Read tool downsamples large images, so this happens on
   wider diagrams — re-render at a higher scale and read that instead:

   ```bash
   npx bpmn-to-image --scale 2 <path-to-bpmn>:.context/verify/<name>.png
   ```

   If it's still not legible, bump `--scale` further and/or crop into left-to-right sections:

   ```bash
   sips -g pixelWidth .context/verify/<name>.png            # image width in px
   # crop a region: sips -c <height> <cropW> --cropOffset <topY> <leftX> <in> --out <out>
   sips -c <height> <cropW> --cropOffset <topY> <leftX> .context/verify/<name>.png --out .context/verify/<name>-1.png
   ```

5. **Review the image for the judgment residue.** Lint already owns the geometric checks, so
   here you focus on what it can't decide — but use the picture to _confirm_ the geometry too
   (a geometric defect visible here that lint did **not** flag is a finding). Work through, in
   order:
   - **Visibility** — is every element the process uses actually drawn? Watch for a flow that
     ends in empty space, a node with no incoming/outgoing arrow, or an obvious gap where a task
     should be — these signal an element that executes but has no shape. _Not_ a defect:
     compensation handlers (a task on a dotted association to a boundary compensation event) and
     event sub-process starts sit off the main sequence-flow chain by design — never flag them
     as orphaned.
   - **No overlaps** — no two shapes (or a shape and a label) sit on top of each other.
   - **No crossing flows** — sequence flows do not cross each other, and no flow runs through a
     shape or over a label.
   - **Spacing & alignment** — consistent gaps; elements on a shared lane line up; the flow
     reads left-to-right. _(judgment — lint can't settle this)_
   - **Readable labels** — every label is legible, near its element, and not clipped.
     _(judgment)_

6. **Report a combined verdict — both nets.** Lead with the overall result, then one section
   per net:

   - **Geometry (bpmnlint):** ✅ clean, or a table of the lint findings (rule + element/flow).
   - **Judgment (rendered image):** a table — one row per issue, each concretely **located**:

     | Element / flow           | What is wrong                                  | Styleguide rule   | Severity |
     | ------------------------ | ---------------------------------------------- | ----------------- | -------- |
     | `Send Welcome Mail` task | overlaps the sub-process border                | No overlaps       | high     |
     | `No` flow                | runs through the `Send Confirmation Mail` task | No crossing flows | high     |

   If both nets are clean, say so explicitly (a single `✅ no issues` row per net is fine) and
   name what each one checked.

7. **Fixing is a separate skill.** When there are findings, hand them to **`/fix-model-layout`**
   — it resolves them DI-only (shape **and** its edge waypoints **and** its label move together)
   so the executable process is provably unchanged. After any fix, **re-run both nets** (re-lint
   _and_ re-render from step 2) until both are clean — a fix that adds a new finding is not a
   fix — and run the process's integration test if there is one (it deploys the BPMN and so also
   validates it parses).

8. **Clean up.** Delete the rendered PNGs from `.context/verify/` once you are done inspecting
   them.

If anything is unclear (which file, what "done" looks like), ask before proceeding.
