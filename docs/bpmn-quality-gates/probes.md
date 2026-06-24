# BPMN quality-gate probes

The probes that back the two safety nets described in [`README.md`](./README.md). Each is a
deliberately broken copy of the membership process that isolates one failure, so you can see
exactly which net catches it. The originals (`services/.../bpmn/membership.bpmn`) are left
intact.

## The probes

Each probe is a self-contained folder — its model, its rendered image, and a **README with the
before/after lint output**:

| Probe                                          | What was broken                                                                                                                                   | Caught by                                     |
| ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------- |
| [`clean`](./probes/clean/)                     | nothing — control; an exact copy of the production membership model, which itself lints **0 problems**.                                           | — (passes both nets)                          |
| [`probe-invisible`](./probes/probe-invisible/) | the `BPMNShape` for **Send Confirmation Mail** was deleted. The task still exists in the XML and still executes — it just never renders.          | bpmnlint `no-bpmndi` (error)                  |
| [`probe-messy`](./probes/probe-messy/)         | the **No** flow was re-routed straight through the _Send Confirmation Mail_ task and the _Membership rejected_ end event. **No shape was moved.** | bpmnlint `local/flow-through-element` (error) |
| [`probe-crossing`](./probes/probe-crossing/)   | the **Welcome → Activated** flow was re-routed to dip down and cross the independent **Re-Send → Mail sent again** flow.                          | bpmnlint `local/no-crossing-flows` (error)    |

## Reproduce

```bash
npm --prefix tools install        # once (installs bpmnlint + the local rule plugin)

# deterministic linter — all four probes
npm --prefix tools run lint:bpmn:probes
# or one at a time:
npx bpmnlint docs/bpmn-quality-gates/probes/probe-messy/probe-messy.bpmn

# visual review (render + multimodal review against the styleguide)
/verify-model-visually docs/bpmn-quality-gates/probes/probe-messy/probe-messy.bpmn
```

## Results — the deterministic net

bpmnlint now catches **all four** probes. Geometry is the linter's job; that is the whole
design.

| Probe             | bpmnlint result                                   |
| ----------------- | ------------------------------------------------- |
| `clean`           | ✅ 0 problems (exit 0)                            |
| `probe-invisible` | ✅ `error no-bpmndi` (exit 1)                     |
| `probe-messy`     | ✅ `error local/flow-through-element` ×2 (exit 1) |
| `probe-crossing`  | ✅ `error local/no-crossing-flows` (exit 1)       |

Each probe's own README shows its full before/after output — see
[`probe-messy`](./probes/probe-messy/) and [`probe-crossing`](./probes/probe-crossing/) (both
passed silently before the custom rules → now `error`).

All four geometry findings are **errors** that fail the gate. We treat geometry uniformly — a
crossing or a flow through a shape is a defect to fix, consistent with `no-overlapping-elements`
(which we also bumped from bpmnlint's default `warn` to `error`). If a crossing is genuinely
unavoidable in a dense diagram, suppress that one occurrence with a `bpmnlint-disable` directive
rather than relaxing the rule globally.

> **History.** `probe-messy` used to be bpmnlint's documented blind spot — the linter's
> shipped rules only compare shape-vs-shape bounds and never inspect edge geometry, so the
> re-routed flow passed. The custom rule below closes that gap, which is why `probe-messy` now
> fails the linter rather than only the visual review.

## Custom layout rules

`bpmnlint:recommended` only touches the DI in two rules: `no-bpmndi` (a semantic element with
no shape) and `no-overlapping-elements` (shape-vs-shape bounding-box overlap, which our
`tools/.bpmnlintrc` bumps from `warn` to **`error`**). Neither looks at **edge waypoints**, so
crossing flows and flows-through-shapes slipped through. Two small in-repo rules close that:

| Rule                         | Level | Detects                                                                                                    |
| ---------------------------- | ----- | ---------------------------------------------------------------------------------------------------------- |
| `local/flow-through-element` | error | a sequence flow whose drawn path enters the interior of a shape it does not connect to                     |
| `local/no-crossing-flows`    | error | two sequence flows whose drawn paths cross (flows that share a node are expected to meet and are excluded) |

They live in [`tools/bpmnlint-plugin-local/`](../../tools/bpmnlint-plugin-local) as an in-repo
bpmnlint plugin — wired through `tools/package.json` as a `file:` devDependency and referenced from
`tools/.bpmnlintrc`, so they run under the same `npm --prefix tools run lint:bpmn` gate with **no npm publishing**.
Each is pure geometry over `dc:Bounds` + edge `waypoint`s, modelled on core's
`no-overlapping-elements.js`. They are deliberately conservative — comparison is scoped per
diagram plane (so collapsed-sub-process drill-downs never collide), expanded containers
(sub-processes, pools, lanes, groups), boundary events, decorative artifacts (text annotations,
data objects/stores), and a flow's own endpoints/hosts are excluded, and container membership
uses moddle inheritance (`$instanceOf`, so `bpmn:Transaction` etc. count). The production model
and `clean.bpmn` stay at **zero false positives**. (These exclusions were hardened by an
adversarial review — see the PR.)

**Known non-goals**, left to the visual review by design: collinear/overlapping flows, a flow
over a text _label_, message-flow/association routing, and — within a single collaboration
plane — a crossing between two different pools. Documented gaps, not silent ones.

## Where the line falls: deterministic vs. judgment

The probes all land on the **deterministic** side. That is most of what people call "layout
chaos", and it belongs in the linter:

**Deterministic — a coordinate formula decides it (linter):** invisible element · shape
overlap · edge crossings · edge through a shape · non-orthogonal segments · off-grid
coordinates · right-to-left (backward) flow · label-over-shape/edge.

**Subjective — depends on meaning or perception (visual review):** semantic grouping (are
related activities clustered the way the domain expects?) · does the layout _communicate
intent_ (happy path prominent, exceptions subordinate?) · cramped-but-valid "breathing room" ·
label _quality_ (not just presence) · whether a flagged crossing is the least-bad option ·
recognising intentional idioms (e.g. a compensation handler attached by a dotted association —
the false positive we hit, see below).

## The visual review's remaining job

Because the deterministic net now owns geometry, `/verify-model-visually` is reserved for the
subjective list above — the judgment calls no coordinate rule can settle. (It still _sees_
overlaps and crossings as defence-in-depth, but it is no longer the only thing standing
between a crossed flow and `main`.)

The skill was validated by a **blind, two-trial** review: each probe was copied to a neutral
name so reviewers could not infer intent from the filename, two independent reviewers per
model executed the skill end-to-end (render → read image → review), and the name↔probe mapping
was reshuffled between trials. Result: `clean` passed, the broken probes were caught, and every
reviewer located the actual defect. Trial 1 surfaced one **false positive** — a reviewer read
the legitimate _Revoke Claim_ compensation handler (a task on a dotted association, off the
sequence-flow chain) as "orphaned". That was a gap in the styleguide, which never told the
reviewer that associations/compensation handlers sit outside the flow by design; it was patched
(see the _Visibility_ note under
[Layout Guidelines](../bpmn-styleguide/styleguide.md#layout-guidelines), which the skill loads,
plus the matching caveat in the skill's own visibility check), and trial 2 returned a clean
pass. The visual loop caught a flaw in its own rules.

> We intentionally ship **no probe for the subjective residue.** A subjective verdict is fuzzy
> by nature — that is exactly why it needs a human/visual judgment rather than a fixture. A
> candidate "cramped-but-valid" probe could be added later (see the open options in the PR).

## `clean.bpmn` and the production model

`clean.bpmn` is an **exact copy** of the production
`services/example-service/src/main/resources/bpmn/membership.bpmn` — both lint **0 problems**.
There is no longer any difference to call out: the two issues the linter originally caught in
the membership model — a missing label on the compensation boundary event and an implicit
`fake-join` on _Revoke Membership Request_ — have been fixed **in the production model itself**
(the label, plus an explicit merge gateway). The clean model is the service default, and this
copy is the control that proves the gates stay quiet on it.
