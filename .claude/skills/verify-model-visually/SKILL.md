---
name: verify-model-visually
description: Use while modeling a BPMN process to visually review a model you just edited. Renders the .bpmn to an image and checks the rendered picture (never the XML) against a visual styleguide — catching invisible elements, overlaps, crossing flows, and messy or hard-to-read layout. Use after editing a model, or when asked to review the diagram, check the layout, or verify the model looks right.
allowed-tools: Read, Bash, Glob
---

# Skill: verify-model-visually

Give the agent the visual feedback loop it lacks. BPMN bugs at the _visual_ layer
(BPMNDI) are invisible in the XML: an element can execute but never render, or a shape
can sit at guessed coordinates that overlap a neighbour or make a flow cross a label.
The only reliable way to catch these is to **look at the rendered picture**.

This skill is the **judgment** complement to the deterministic `bpmnlint` linter
(`npm --prefix tools run lint:bpmn`). The linter now owns the geometric checks — invisible elements,
overlapping shapes, crossing flows, and flows routed through a shape are all caught
deterministically. Run the linter first; run this skill for what geometry can't decide:
whether the layout communicates intent, semantic grouping, cramped-but-valid spacing, and
label quality. (A geometric issue spotted here means the linter missed one — worth noting.)

## Key Rules

- **Review the rendered image, not the XML.** Never reason about layout from `<dc:Bounds>`
  or waypoints. Open the PNG and judge what a human sees.
- **Check the model against the styleguide's Layout Guidelines.** Load
  `docs/bpmn-styleguide/styleguide.md` (the _Layout Guidelines_ section) each run; do not rely on
  memory.
- **Report concrete, _located_ issues.** Name the element or flow and the rule it breaks
  ("the `Send Welcome Mail` task overlaps the sub-process border" / "the `No` flow crosses
  the `Send Confirmation Mail` task"). Never give a vague verdict like "looks a bit off".
- **If everything passes, say so explicitly** and list what you checked.

## Instructions

1. **Resolve the `.bpmn` file.** Use the path the user gave. If none, `Glob` for
   `**/*.bpmn` (production models live under `services/**/src/main/resources/bpmn/`). If more
   than one matches and the target is ambiguous, ask which file to review before continuing.

2. **Render the model to PNG** with bpmn-to-image
   ([bpmn-io/bpmn-to-image](https://github.com/bpmn-io/bpmn-to-image)) — the same tool the
   `bpmn-export` skill uses, so no new dependency. Write to a scratch path:

   ```bash
   npx bpmn-to-image <path-to-bpmn>:.context/verify/<name>.png
   ```

   If the render command fails, stop and report the error (a model that will not render is
   itself a finding).

3. **Load the inputs.** `Read` the rendered PNG (it loads as an image) and `Read`
   `docs/bpmn-styleguide/styleguide.md` (the project BPMN styleguide — focus on its **Layout
   Guidelines** section; the rest covers naming, IDs, and automation).

   If labels are too small to read — the Read tool downsamples large images, so this happens
   on wider diagrams — re-render at a higher scale and read that instead:

   ```bash
   npx bpmn-to-image --scale 2 <path-to-bpmn>:.context/verify/<name>.png
   ```

   If it's still not legible, bump `--scale` further and/or crop into left-to-right sections:

   ```bash
   sips -g pixelWidth .context/verify/<name>.png            # image width in px
   # crop a region: sips -c <height> <cropW> --cropOffset <topY> <leftX> <in> --out <out>
   sips -c <height> <cropW> --cropOffset <topY> <leftX> .context/verify/<name>.png --out .context/verify/<name>-1.png
   ```

4. **Compare the diagram against the styleguide.** Work through, in order:
   - **Visibility** — is every element that the process uses actually drawn? Watch for a flow
     that ends in empty space, a node with no incoming/outgoing arrow, or an obvious gap where
     a task should be — these signal an element that executes but has no shape. _Not_ a defect:
     compensation handlers (a task on a dotted association to a boundary compensation event) and
     event sub-process starts sit off the main sequence-flow chain by design — never flag them as
     orphaned.
   - **No overlaps** — no two shapes (or a shape and a label) sit on top of each other.
   - **No crossing flows** — sequence flows do not cross each other, and no flow runs through
     a shape or over a label.
   - **Spacing & alignment** — consistent gaps; elements on a shared lane line up; the flow
     reads left-to-right.
   - **Readable labels** — every label is legible, near its element, and not clipped.

5. **Report findings as a table** — one row per issue, each concretely **located** (name the
   element or flow):

   | Element / flow           | What is wrong                                  | Styleguide rule   | Severity |
   | ------------------------ | ---------------------------------------------- | ----------------- | -------- |
   | `Send Welcome Mail` task | overlaps the sub-process border                | No overlaps       | high     |
   | `No` flow                | runs through the `Send Confirmation Mail` task | No crossing flows | high     |

   If the diagram passes, say so explicitly (a single `✅ no issues` row is fine) and name the
   checks you ran. Note that this is an aesthetic review and recommend `npm --prefix tools run lint:bpmn` for
   the structural pass.

   When you fix a layout issue, edit the DI coordinates in the `.bpmn` and shift whole groups
   consistently — the shape _and_ its edge waypoints _and_ its label move together. After any DI
   edit, re-render from step 2 to confirm, and run the process's integration test if there is one
   (it deploys the BPMN and so also validates it parses).

6. **Clean up.** Delete the rendered PNGs from `.context/verify/` once you are done inspecting them.

If anything is unclear (which file, what "done" looks like), ask before proceeding.
