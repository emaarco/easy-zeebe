# bpmn-fix

Deterministic, **layout-preserving** fixers for the geometry problems the linter
(`npm --prefix tools run lint:bpmn`) and the `/verify-model-visually` skill detect. Part of the
project's BPMN quality gates — see [`docs/bpmn-quality-gates/`](../../docs/bpmn-quality-gates/).

> **⚠️ Reference implementation, not a drop-in library.** The fixers below are worked ideas, **not production-tested** tooling. This applies **especially to the automatic fixers** — they edit diagram geometry heuristically, so review every change they make and harden them before relying on them. Fork and adapt freely.

## The safety invariant

Layout bugs live entirely in the **diagram-interchange (`bpmndi:`) layer** — coordinates and
waypoints — never in `bpmn:` semantics. Both tools here touch **only DI**. The executable
process (token flow, sequence flows, elements) is therefore **provably unchanged**: re-running
the process tests after a fix is a formality, not a risk.

These are two of the three fix variants. The third — letting the **AI** edit the DI guided by
the lint findings + the rendered image — is the [`fix-model-layout`](../../.claude/skills/fix-model-layout)
skill, which orchestrates all three.

## `fix.mjs` — Tier 1: surgical reroute (ManhattanLayout)

Reuses the **same geometry** the lint rules use to _locate_ the broken edges, then recomputes
clean orthogonal waypoints for **just those edges** with bpmn.io's own
`diagram-js` `ManhattanLayout.connectRectangles`. No shape is moved; only the offending edge's
waypoints change.

```bash
npm --prefix tools run fix:bpmn -- <file.bpmn>            # dry run: report what it would reroute
npm --prefix tools run fix:bpmn -- <file.bpmn> --write    # apply in place
```

Handles **`flow-through-element`** (flow routed through a shape) and **`no-crossing-flows`**
(crossing flows). For each flagged flow it re-routes between the source and target shapes, then
**verifies** the new route actually clears the obstacles — `ManhattanLayout` routes directly and
does _not_ avoid obstacles, so if the clean route still hits a shape the tool **escalates**
(leaves the edge untouched and reports it) rather than guessing. Escalated cases go to the AI
path or to full auto-layout.

Known limits: it re-routes edges but does not reposition their **labels**; it does not resolve
shape **overlaps** or place **missing shapes** (those are not pure edge problems).

## `layout.mjs` — Tier 3: full auto-layout (escape hatch)

Throws the DI away and regenerates a complete left-to-right layout from the semantic model with
[`bpmn-auto-layout`](https://github.com/bpmn-io/bpmn-auto-layout). Use it only for models with
no/garbage DI, or as a last resort.

```bash
npm --prefix tools run auto-layout:bpmn -- <file.bpmn>            # writes <file>.laidout.bpmn
npm --prefix tools run auto-layout:bpmn -- <file.bpmn> --write    # overwrite in place
```

**It discards hand-tuned positioning** and has hard limits (only the first participant of a
collaboration; sub-processes rendered collapsed; **groups, text annotations, associations and
message flows are not laid out** — e.g. on the membership model it drops the compensation
`Association`, which then trips `no-bpmndi`). Great for "lay out a model that has none", wrong
for "nudge only the broken bit".

## Files

```
fix.mjs       Tier 1 surgical reroute (reuses ../bpmnlint-plugin-local/rules/_geometry.js)
layout.mjs    Tier 3 full auto-layout (bpmn-auto-layout)
```
