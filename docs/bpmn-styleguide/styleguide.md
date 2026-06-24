# BPMN Styleguide

This guide defines the conventions for BPMN modeling & automation in Easy Zeebe.
It is divided into three parts:

- **Modelling** — naming conventions, layout guidelines, and element choices
- **Technical Configuration** — element IDs, message IDs, and worker IDs
- **Automation** — process variable design and worker patterns

---

## Table of Contents

- [Modelling](#modelling)
  - [Naming Conventions](#naming-conventions)
  - [Layout Guidelines](#layout-guidelines)
  - [Notation Elements](#notation-elements)
- [Technical Configuration](#technical-configuration)
  - [Element IDs](#element-ids)
  - [Message IDs](#message-ids)
  - [Worker IDs](#worker-ids)
- [Automation](#automation)
  - [Process Variables](#process-variables)
  - [Worker Design](#worker-design)

---

## Modelling

The business modeling layer lays the foundation for understandable and clearly structured BPMN diagrams.
The goal is to design processes that are logical, traceable, and easy for all stakeholders to read.

All BPMN elements are permitted — the recommendations below guide you towards the best element for each
use case.

### Naming Conventions

Clear, consistent naming ensures that BPMN models are easy to understand and maintain.
Names should be intuitive and reflect the purpose of the element.

**Tasks** — describe what must be done. Use exactly one noun and one verb where possible.

> Example: `Check Order`, `Send Confirmation`, `Validate Payment`

**Events** — describe what happens. Use a noun and a passive or past-tense verb.

> Example: `Invoice Received`, `Order Confirmed`, `Timer Elapsed`

**Gateways** — summarize the decision being made as a short question. Ideally one noun and one verb.

> Example: `Customer Known?`, `Payment Successful?`, `Stock Available?`

**Pools and Lanes** — use descriptive role or system names.

> Example: `Customer System`, `Order Service`, `User`

### Layout Guidelines

A clean layout makes the difference.
A readable diagram helps all stakeholders understand and analyze processes quickly.
These are also the conventions the [`/verify-model-visually`](../../.claude/skills/verify-model-visually)
skill reviews a _rendered_ model against.

- **Reading direction**: model from left to right to support the natural reading flow — start on the
  left, end on the right.
- **Happy path prominent**: keep the main (happy) path running straight across the middle, and place
  exceptions and alternative branches visually subordinate above or below it. The arrangement should
  tell the right story at a glance; backward segments are only acceptable for obvious loops.
- **Spacing & breathing room**: leave comfortable space between elements so the diagram stays clear and
  uncluttered. Even a region where nothing technically overlaps should not _read_ as cramped.
- **Alignment & even spacing**: line up elements that share a row or lane on one axis instead of letting
  them drift; keep gaps along a path roughly uniform and distribute parallel branches evenly.
- **No overlaps**: no two shapes — and no label on top of a shape — sit on one another. (A boundary
  event on its host's border is intended, not an overlap.)
- **Avoid crossings & through-routing**: sequence flows should not cross each other or run through a
  shape. Keep flows orthogonal with clean right-angle bends; use intermediate events or sub-processes to
  reduce complexity when flows would otherwise intersect.
- **Semantic grouping**: cluster related activities the way the domain expects (a sub-process's steps
  should read as one unit); don't cram unrelated elements together just because the coordinates allow it.
- **Readable labels**: every label is legible, sits next to its element, is not clipped or stranded in
  empty space, and actually says something meaningful (see [Naming Conventions](#naming-conventions)).
- **Visibility**: every element the process uses is actually drawn — there is no element that executes
  but has no shape. Compensation handlers (a task on a **dotted association** to a boundary compensation
  event) and event sub-process starts sit _off_ the main sequence-flow chain by design — that is
  intentional, not a missing or orphaned element.

### Notation Elements

There are no restrictions on which elements you may use.
The following recommendations help you choose the right element for your use case.

**User Task** — when a task is explicitly assigned to a specific person.

> Example: The user enters the best-before date of a scanned item.

**Message Catch Event** — when waiting for an external input (fire-and-forget, no concurrent handling needed).

> Example: An event that reacts to an incoming message after the process has moved on.

**Message Receive Task** — when additional requirements on message handling are needed, such as allowing
concurrent cancellation while waiting.

> Example: Waiting for an order confirmation while allowing the order to be canceled in parallel via a
> boundary event.

---

## Technical Configuration

Technical modeling builds on business modeling and focuses on automation and technical implementation.
Clear conventions for IDs, message names, and type IDs help maintain an overview even in complex processes.

### Element IDs

Unique, readable IDs make BPMN diagrams easier to maintain and analyse.
Every element relevant to automation should have an ID following the format `Type_Name`,
where both `Type` and `Name` are written in CamelCase.

The table below is illustrative — it shows the pattern for common element types, not an exhaustive list.
Apply the same `Type_Name` structure to any element not listed here.

| Element              | ID Convention             | Example                          |
| -------------------- | ------------------------- | -------------------------------- |
| Start Event          | `startEvent_State`        | `startEvent_SubscriptionStarted` |
| Intermediate Event   | `event_State`             | `event_ConfirmationReceived`     |
| End Event            | `endEvent_State`          | `endEvent_SubscriptionCompleted` |
| Service Task         | `serviceTask_Description` | `serviceTask_SendWelcomeMail`    |
| User Task            | `userTask_Description`    | `userTask_ReviewSubscription`    |
| Message Receive Task | `receiveTask_Description` | `receiveTask_AwaitConfirmation`  |
| Gateway              | `gateway_Description`     | `gateway_IsCustomerKnown`        |

### Message IDs

Message IDs are unique identifiers for messages. To ensure uniqueness across services, use the following
schema — both parts in CamelCase:

```
<serviceName>.<state>
```

> Example: `miravelo.membershipConfirmed`, `orderService.orderPlaced`

### Worker IDs

A worker ID (also called a "job type") is the topic that a Zeebe service task publishes and a job worker
subscribes to. To ensure uniqueness across services, use the following schema — both parts in CamelCase:

```
<serviceName>.<elementIdWithoutTypePrefix>
```

> Example: if the service task ID is `serviceTask_SendWelcomeMail` in the `miravelo` service, the
> type ID is `miravelo.sendWelcomeMail`.

---

## Automation

Implementing processes with Zeebe brings the model to life. This section covers how to design workers
efficiently and robustly so they interact reliably with the engine.

### Process Variables

Process variables should be kept to a minimum. **The process engine is not a database.**

Pass only the information that is genuinely necessary for orchestration decisions. Avoid storing full
domain objects as process variables.

> Good: a boolean flag `isPremiumCustomer` or a UUID `subscriptionId`
>
> Avoid: a complete customer JSON object or a list of all order items

### Worker Design

Workers should use typed input classes when they consume multiple variables. This improves readability
and reduces the risk of variable name typos.

- Use `@Variable` for a single injected variable.
- Use `@VariableAsType` with a typed `data class` when multiple variables are needed.

**Example** (Easy Zeebe style with `@JobWorker` annotation):

```kotlin
@Component
class SendWelcomeMailWorker(
    private val sendWelcomeMailUseCase: SendWelcomeMailUseCase,
) {
    @JobWorker(type = MiraveloMembershipProcessApi.TaskTypes.MIRAVELO_SEND_WELCOME_MAIL)
    fun handle(@Variable membershipId: UUID) {
        sendWelcomeMailUseCase.sendWelcomeMail(MembershipId(membershipId))
    }
}
```

When multiple variables are required, use a typed input class:

```kotlin
@Component
class ProcessOrderWorker(
    private val processOrderUseCase: ProcessOrderUseCase,
) {
    @JobWorker(type = OrderProcessApi.TaskTypes.ORDER_PROCESS_ORDER)
    fun handle(@VariableAsType input: Input) {
        processOrderUseCase.processOrder(
            orderId = OrderId(input.orderId),
            isPremiumCustomer = input.isPremiumCustomer,
        )
    }

    data class Input(
        val orderId: String,
        val isPremiumCustomer: Boolean,
    )
}
```

All type constants (`@JobWorker(type = ...)`) must reference the generated `ProcessApi` object.
Never use raw string literals.
See the [bpmn-to-code](https://github.com/Miragon/bpmn-to-code) docs for how the `ProcessApi` is generated from a BPMN.
