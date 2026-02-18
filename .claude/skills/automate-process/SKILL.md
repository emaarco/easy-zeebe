---
name: automate-process
description: Scaffold full hexagonal glue-code (workers, use-case interfaces, application services, and process adapter). Accepts a ProcessApi file, a BPMN model, or a plain description. Use when the user wants to generate a complete hexagonal layer for a new Zeebe process.
argument-hint: "[<path-to-ProcessApi-or-BPMN-file>]"
allowed-tools: Read, Write, Glob
---

# Skill: automate-process

Generate full hexagonal-architecture glue-code for automating a Zeebe BPMN process:
job workers, process out-adapter, inbound/outbound port interfaces, and application service stubs.

> **Note:** The generated code follows the hexagonal architecture used in this project.
> If your project uses a different architecture, treat the output as a starting point
> and adapt the package structure, naming conventions, and layering to your needs.

## Usage

```
/automate-process [<path-to-ProcessApi-or-BPMN-file>]
```

Examples:

```
# From a ProcessApi file
/automate-process services/example-service/src/main/kotlin/io/miragon/example/adapter/process/NewsletterSubscriptionProcessApi.kt

# From a BPMN file — skill finds the ProcessApi automatically
/automate-process services/example-service/src/main/resources/bpmn/newsletter.bpmn

# No arguments — skill searches for ProcessApi files and asks which one to use
/automate-process
```

## What This Skill Creates

| Layer                | Location                     | What                                     |
|----------------------|------------------------------|------------------------------------------|
| Inbound workers      | `adapter/inbound/zeebe/`     | One `@JobWorker` class per service task  |
| Outbound adapter     | `adapter/outbound/zeebe/`    | One process out-adapter class            |
| Inbound ports        | `application/port/inbound/`  | One use-case interface per worker        |
| Outbound port        | `application/port/outbound/` | One process port interface               |
| Application services | `application/service/`       | One service class per use-case interface |

The adapter and worker files reference these interfaces so the hexagonal wiring is complete from the start.

## CRITICAL Guardrail

**All string constants must come from the ProcessApi — never use raw string literals.**

- Worker `@JobWorker(type = ...)` → `ProcessApi.TaskTypes.CONSTANT`
- Adapter `startProcess(processId = ...)` → `ProcessApi.PROCESS_ID`
- Adapter `sendMessage(messageName = ...)` → `ProcessApi.Messages.CONSTANT`
- Variable keys → `ProcessApi.Variables.CONSTANT` where defined

## Instructions

### Step 0 – Validate the BPMN model first

Before generating any code, offer to run the `review-process` subagent to validate the BPMN model:

1. Ask the user: *"Would you like me to run the review agent first to validate the BPMN model before generating
   glue-code?"*
2. If they agree, invoke the `review-process` subagent on the BPMN file or the process identified from `$ARGUMENTS`.
3. If the review surfaces issues, present them to the user and wait for confirmation that they have been resolved before
   continuing.
4. If the user skips the review, proceed directly to Step 1.

### Step 1 – Resolve the input source

Determine where the process constants come from based on `$ARGUMENTS`:

- **ProcessApi file (`.kt`)**: read it directly. Extract: package name, object name, `PROCESS_ID`, all `TaskTypes.*`,
  `Messages.*`, `Signals.*` (if present), and `Variables.*` constants.
- **BPMN file (`.bpmn`)**: search the same service module for a `*ProcessApi.kt` file (Glob
  `**/adapter/process/*ProcessApi.kt`). If found, read it as above. If not found, ask the user whether to continue
  without type-safe constants.
- **No argument / plain description**: search the whole codebase for `*ProcessApi.kt` files (Glob
  `**/adapter/process/*ProcessApi.kt`). List them and ask the user which one to use.

If you can't find a ProcessApi, ask the user to generate one first.

### Step 2 – Determine packages and source root

Derive the base package from the ProcessApi package. Example:

- ProcessApi: `io.miragon.example.adapter.process`
- Base: `io.miragon.example`
- Inbound workers: `<base>.adapter.inbound.zeebe`
- Outbound adapter: `<base>.adapter.outbound.zeebe`
- Inbound ports: `<base>.application.port.inbound`
- Outbound port: `<base>.application.port.outbound`
- Services: `<base>.application.service`

### Step 2.5 – Discover existing domain types

Before generating any port or adapter code, scan the domain package (`<base>.domain`) for existing value objects.

For each `Variables.*` constant in the ProcessApi:

- Derive a candidate domain type name by converting the constant to PascalCase
  (e.g. `NEWSLETTER_ID` → `NewsletterId`, `SUBSCRIPTION_ID` → `SubscriptionId`)
- If a variable name makes no sense as a domain type name, ask the developer for feedback
- Check if a file with that name exists under `**/domain/<TypeName>.kt`
- If found, read it and note the class name and its primary constructor parameter type (typically `UUID` or `String`)

If no suitable domain type exists for a variable, **create one** (a Kotlin `data class` wrapping `UUID` or `String`,
with a secondary `String` constructor when the primary wraps `UUID`) **before** generating ports or adapters.

### Step 3 – Generate inbound port interfaces (use cases)

For each `TaskTypes.*` constant, generate a use-case interface. Name convention: derive a meaningful interface name from
the task type constant (e.g. `NEWSLETTER_ABORT_REGISTRATION` → `AbortSubscriptionUseCase`).

Generated structure:

- Interface with one method matching the worker's action
- Method parameters must use domain types discovered in Step 2.5 — never raw `String` or `UUID`

Skip if file already exists.

### Step 4 – Generate outbound port interface

Generate one outbound port interface for the process. Name convention: derive from the process name (e.g.
`NewsletterSubscription` → `NewsletterSubscriptionProcess`).

Generated structure:

- `startProcess` method (returns `Long`) — or a message-send method if the process uses a message start event
- One method per `Messages.*` constant
- All method parameters use domain types discovered in Step 2.5

Skip if file already exists.

### Step 5 – Generate application services

For each inbound use-case interface, generate an application service implementing it. Name convention: append `Service`
to the use-case name (e.g. `AbortSubscriptionUseCase` → `AbortSubscriptionService`).

Generated structure:

- `@Service` class implementing the use-case interface
- `override fun` with domain type parameters (matching the use-case interface) and `TODO("Add implementation")` body

Skip if file already exists.

### Step 6 – Generate workers (with use-case injection)

Same as `/create-worker` Step 3, but inject the generated use-case interface as the constructor parameter instead of
a placeholder. Apply the same `@Variable` / `@VariableAsType` guidance from that step.

Worker `handle` method parameters must be **primitive types** (`UUID`, `String`) annotated with `@Variable`;
convert them to domain types inside the body before calling the use case
(e.g. `useCase.handle(NewsletterId(newsletterId))`).

### Step 7 – Generate process out-adapter (implementing outbound port)

Same as `/create-process-adapter` Step 3, but add `: ProcessNameProcess` to the class declaration so it implements the
outbound port interface. Mark each method with `override`.

Adapter methods must accept **domain types** as parameters. Inside the method body, extract the primitive value
(e.g. `id.value.toString()`) before passing to `engineApi.startProcess` / `engineApi.sendMessage`.

### Step 8 – Report

List every file created or skipped. Remind the developer to:

1. Run `/test-worker` for each worker
2. Run `/test-process-adapter` for the adapter
3. Run `/test-process` to generate process integration tests

Offer to run these test-generation commands right away.