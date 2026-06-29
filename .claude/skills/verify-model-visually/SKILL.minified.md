---
name: verify-model-visually
description: Use while modeling a BPMN process to review a model you just edited. Runs both quality nets — the deterministic bpmnlint geometry linter and a visual review of the rendered image — and reports a combined verdict. Catches invisible elements, overlaps, crossing flows, flows routed through a shape (lint), plus messy or hard-to-read layout that needs judgment (image). [...]
---

# Skill: verify-model-visually

Give the agent the feedback loop it lacks. BPMN bugs at the _visual_ layer (BPMNDI) are invisible in the XML: an element can execute but never render, or a shape can sit at guessed coordinates that overlap a neighbour or make a flow cross a label. None of this shows up in a code diff. [...]

A BPMN model is guarded by **two complementary nets, and this skill runs both**:

- **bpmnlint** (`npm --prefix tools run lint:bpmn`) — the **deterministic** net: invisible elements, overlapping shapes, crossing flows, flows routed through a shape. Fast, exact, no judgment. [...]
- **the rendered image** — the **judgment** net: intent, semantic grouping, cramped-but-valid spacing, label quality. [...]

Run lint first, then look at the picture. The linter is also wired into the pre-commit hook (`npm --prefix tools run hooks:install`) and CI; running it here makes this a **complete** review, not just the image half. A geometric defect the image surfaces that lint missed is itself a finding. [...]

## Key Rules

- Run both nets and report both — green lint + ugly picture still fails; clean picture + red lint still fails. [...]
- Review the rendered image, not the XML. [...]
- Check against the **Layout Guidelines** in `docs/bpmn-styleguide/styleguide.md` — load it each run. [...]
- Report concrete, _located_ issues (element/flow + rule broken), never vague verdicts. [...]
- If all pass, say so explicitly + name what each net checked. [...]

## Instructions

1. **Resolve the `.bpmn`** — use the given path; else `Glob` `**/*.bpmn`; ask if ambiguous. [...]
2. **Lint first (geometry net)** — `npm --prefix tools run lint:bpmn` (globs all production models). Clean = silent, exit 0; non-zero = capture each finding (element/flow + rule) but **don't stop** — still do the visual pass. Missing binary → `npm --prefix tools install` once. **Skip only when geometry was already gated upstream this run** (CI's dedicated lint job, or just after the pre-commit hook) — then go straight to the visual pass + note lint ran upstream. [...]
3. **Render to PNG** — `npx bpmn-to-image <bpmn>:.context/verify/<name>.png` (reuses `bpmn-export`). On failure, stop and report. [...]
4. **Load inputs** — `Read` the PNG (loads as image) + `docs/bpmn-styleguide/styleguide.md` (Layout Guidelines). Tiny labels → re-render `--scale 2`, else crop with `sips`. [...]
5. **Review the image for the judgment residue** — confirm geometry + judge: visibility / no overlaps / no crossing flows / spacing & alignment / readable labels. A geometric defect lint didn't flag is a finding. [...]
6. **Report a combined verdict** — overall result, then **Geometry (bpmnlint)** ✅/findings-table and **Judgment (image)** table (`element/flow | what's wrong | rule | severity`, one located row each). Both clean → say so + name checks. [...]
7. **Fixing → `/fix-model-layout`** (DI-only: shape + waypoints + label move together). After any fix, re-run **both** nets (re-lint + re-render) until clean, run the integration test if one exists. [...]
8. **Clean up** — delete rendered PNGs from `.context/verify/`. [...]

[...] If anything is unclear, ask before proceeding.
