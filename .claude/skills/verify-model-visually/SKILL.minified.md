---
name: verify-model-visually
description: Use while modeling a BPMN process to visually review a model you just edited. Renders the .bpmn to an image and checks the rendered picture (never the XML) against a visual styleguide — catching invisible elements, overlaps, crossing flows, and messy or hard-to-read layout. Use after editing a model, or when asked to review the diagram, check the layout, or verify the model looks right. [...]
---

# Skill: verify-model-visually

Give the agent the visual feedback loop it lacks. BPMN bugs at the _visual_ layer (BPMNDI) are invisible in the XML: an element can execute but never render, or a shape can sit at guessed coordinates that overlap a neighbour or make a flow cross a label. The only reliable way to catch these is to **look at the rendered picture**.

This skill is the **judgment** complement to the deterministic `bpmnlint` linter (`npm --prefix tools run lint:bpmn`). The linter now owns the geometric checks — invisible elements, overlapping shapes, crossing flows, and flows routed through a shape are all caught deterministically. Run the linter first; run this skill for what geometry can't decide: whether the layout communicates intent, semantic grouping, cramped-but-valid spacing, and label quality. [...]

## Key Rules

- Review the rendered image, not the XML. [...]
- Check the model against the **Layout Guidelines** in `docs/bpmn-styleguide/styleguide.md` — load it each run. [...]
- Report concrete, _located_ issues (element/flow + rule broken), never vague verdicts. [...]
- If all pass, say so explicitly. [...]

## Instructions

1. **Resolve the `.bpmn`** — use the given path; else `Glob` `**/*.bpmn`; ask if ambiguous. [...]
2. **Render to PNG** — `npx bpmn-to-image <bpmn>:.context/verify/<name>.png` (reuses `bpmn-export`). On failure, stop and report. [...]
3. **Load inputs** — `Read` the PNG (loads as image) + `docs/bpmn-styleguide/styleguide.md` (Layout Guidelines). [...]
4. **Compare vs. styleguide** — visibility / no overlaps / no crossing flows / spacing & alignment / readable labels. [...]
5. **Report as a table** (`element/flow | what's wrong | rule | severity`, one located row per issue); if clean, say so + name checks; note this is aesthetic, recommend `npm --prefix tools run lint:bpmn`. On fix: shift whole DI groups (shape + waypoints + label) together, re-render, run integration test. [...]
6. **Clean up** — delete rendered PNGs from `.context/verify/`. [...]

[...] If anything is unclear, ask before proceeding.
