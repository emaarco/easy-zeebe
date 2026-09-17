# The AI edits the DI

Not every detected problem is an edge-routing problem. Here `Send Rejection Mail` was moved on
top of the `Membership rejected` end event — an **overlap**.

## Before ([`before.bpmn`](./before.bpmn))

![before](./before.png)

The overlap is flagged by a **standard** bpmnlint rule (not one of our custom edge rules):

```
serviceTask_SendRejectionMail  error  Element overlaps with other element  no-overlapping-elements
endEvent_MembershipRejected    error  Element overlaps with other element  no-overlapping-elements
✖ 2 problems (2 errors, 0 warnings)                  # exit 1
```

An overlap can't be resolved by re-routing edges — the **repair** is to move a shape, and its
connected edges + label with it:

This is exactly why the hand DI-edit exists: the overlap is **deterministically detected**, but
the repair is not an edge-routing problem. The detection isn't the hard part — the fix is.

## After ([`after.bpmn`](./after.bpmn))

The agent nudges `Send Rejection Mail` clear of the end event; its incoming flow follows.

![after](./after.png)

```
$ npx bpmnlint after.bpmn
                                                     # ✅ 0 problems — exit 0
```

Still DI-only — the process is unchanged. (The same hand-edit path handles a missing shape
(`no-bpmndi`), a flow routed through a shape or a crossing flow, or a layout that is _valid but
reads badly_, which no rule flags at all.)
