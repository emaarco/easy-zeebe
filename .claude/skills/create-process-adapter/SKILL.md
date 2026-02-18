---
name: create-process-adapter
argument-hint: "[<path-to-ProcessApi-or-BPMN-file>]"
allowed-tools: Read, Write, Glob
description: Scaffold or update a Zeebe process out-adapter. Accepts a ProcessApi file, a BPMN model, or a plain description. Use when the user wants to generate the outbound Zeebe adapter for a BPMN process.
---

# Skill: create-process-adapter

Generate or update a Zeebe process out-adapter (outbound adapter) for a BPMN process.

## What is a Process Adapter?

A process adapter is an **outbound adapter** in hexagonal terms. It lives at the boundary where *your application*
reaches out to the process engine: your domain or application service calls the adapter to initiate or advance a
process instance — start a process, send a correlation message, signal a boundary event, or query state.

This is the mirror of a job worker:

| Direction    | Pattern         | Who initiates the call?                     |
|--------------|-----------------|---------------------------------------------|
| **Inbound**  | Job Worker      | The engine calls your code (or you poll it) |
| **Outbound** | Process Adapter | Your code calls the engine                  |

The process adapter collects all outgoing engine operations for one process in a single class, so the rest of your
codebase depends only on a clean port interface — never on the Camunda SDK directly.

## Usage

```
/create-process-adapter [<path-to-ProcessApi-or-BPMN-file>]
```

Examples:

```
# From a ProcessApi file
/create-process-adapter services/example-service/src/main/kotlin/io/miragon/example/adapter/process/NewsletterSubscriptionProcessApi.kt

# From a BPMN file — skill finds the ProcessApi automatically
/create-process-adapter services/example-service/src/main/resources/bpmn/newsletter.bpmn

# No arguments — skill searches for ProcessApi files and asks which one to use
/create-process-adapter
```

## What This Skill Creates or Updates

- One process out-adapter class
    - Located in `adapter/outbound/zeebe/`
    - methods for `startProcess` (using `PROCESS_ID`)
    - methods per message in `Messages.*` (using `sendMessage`)
    - methods per signal in `Signals.*` (using `sendSignal`), if present

If the adapter file already exists, the skill compares it to the ProcessApi and adds any missing methods or corrects
outdated constant references.

## Pattern

```kotlin
@Component
class NewsletterSubscriptionProcessAdapter(
    private val engineApi: ProcessEngineApi
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId): Long {
        val variables = mapOf(Variables.SUBSCRIPTION_ID to id.value.toString())
        return engineApi.startProcess(
            processId = NewsletterSubscriptionProcessApi.PROCESS_ID,
            variables = variables
        )
    }

    override fun confirmSubscription(id: SubscriptionId) {
        engineApi.sendMessage(
            messageName = Messages.NEWSLETTER_SUBSCRIPTION_CONFIRMED,
            correlationId = id.value.toString(),
        )
    }
}
```

## Key Rules

- All string constants must come from the ProcessApi — never use raw string literals
- `startProcess(processId = ...)` must reference `ProcessApi.PROCESS_ID`
- `sendMessage(messageName = ...)` must reference `ProcessApi.Messages.CONSTANT`; always pass `correlationId` to
  correlate the message with the right process instance
- `sendSignal(signalName = ...)` must reference `ProcessApi.Signals.CONSTANT`
- Variable keys in `startProcess` must reference `ProcessApi.Variables.CONSTANT`
- Method names come from the outbound port interface (not from the ProcessApi); the generated scaffold uses
  placeholder names that match the ProcessApi constant until the developer wires in the port
- Method signatures must accept **domain types** (not raw `String` or `UUID`). Inside the method body, extract
  the primitive with `.value.toString()` (or `.value`) before passing to `engineApi`.

## Instructions

### Step 1 – Resolve the input source

Determine where the process constants come from based on `$ARGUMENTS`:

- **ProcessApi file (`.kt`)**: read it directly. Extract: package name, object name, `PROCESS_ID`, all `Messages.*`
  constants, all `Signals.*` constants (if present), all `Variables.*` constants.
- **BPMN file (`.bpmn`)**: search the same service module for a `*ProcessApi.kt` file
  (Glob `**/adapter/process/*ProcessApi.kt`). If found, read it as above. If not found, ask the user whether to
  continue without type-safe constants.
- **No argument / plain description**: search the whole codebase for `*ProcessApi.kt` files
  (Glob `**/adapter/process/*ProcessApi.kt`). List them and ask the user which one to use.

### Step 2 – Determine the target package and source root

Derive the base package from the ProcessApi package (e.g. `io.miragon.example.adapter.process` → base:
`io.miragon.example`). Adapter package: `<base>.adapter.outbound.zeebe`.

Determine the source root path from the ProcessApi file path (e.g. `services/example-service/src/main/kotlin/`).

### Step 3 – Generate or update the process out-adapter

Derive the adapter class name from the ProcessApi object name (e.g.
`NewsletterSubscriptionProcessApi` → `NewsletterSubscriptionProcessAdapter`).

Check whether a file with that name already exists at the target location.

**If the file does not exist — generate:**

Follow the Pattern above. Concretely:

- `@Component` class injecting `ProcessEngineApi`
- One method that calls `engineApi.startProcess(processId = ProcessApiObject.PROCESS_ID, variables = ...)`, passing
  `ProcessApiObject.Variables.*` keys for each variable; use a conventional placeholder method name (e.g.
  `startProcess`). Method accepts domain types; extract `.value.toString()` for the variables map.
- One method per `Messages.*` constant calling `engineApi.sendMessage(messageName = ProcessApiObject.Messages.CONSTANT,
  correlationId = id.value.toString())`. Method accepts the relevant domain type as parameter.
- One method per `Signals.*` constant calling `engineApi.sendSignal(signalName = ProcessApiObject.Signals.CONSTANT)`
  (skip if no signals are defined)

**If the file already exists — update:**

Read the existing file. Compare each public method against the ProcessApi constants. Add methods for any `Messages.*`
or `Signals.*` constants that are not yet represented. Correct any constant references that are stale or use raw
strings. Preserve existing method bodies and any added behavior.

### Step 4 – Report

List each file created, updated, or skipped (if nothing changed) and remind the developer to:

1. Wire in the outbound port interface if one exists (add `: ProcessNameProcess` and `override` to each method)
2. Run `/test-process-adapter` to generate unit tests for the adapter
