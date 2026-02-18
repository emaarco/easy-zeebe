---
name: create-worker
description: Scaffold or update a @JobWorker class for a service task. Accepts a ProcessApi file, a BPMN model, or a plain description. Use when the user wants to generate an inbound Zeebe adapter for a BPMN service task.
argument-hint: "[<path-to-ProcessApi-or-BPMN-file>] [task-type-constant]"
allowed-tools: Read, Write, Glob
---

# Skill: create-worker

Generate one Zeebe job worker (inbound adapter) for a specific service task.
To generate workers for multiple service tasks, invoke this skill once per task.

## What is a Job Worker?

A job worker is an **inbound adapter** in hexagonal terms.
It lives at the boundary where the process engine reaches into your application:
Zeebe polls your worker for jobs and, when one is available, invokes the `handle` method.
It's much like the engine making a request to your code to perform some work.

The worker translates the Zeebe job (variables, job key) into a domain call on a use-case interface.
After that it completes the job and returns control to the engine.

## Usage

```
/create-worker [<path-to-ProcessApi-or-BPMN-file>] [task-type-constant]
```

Examples:

```
# From a ProcessApi file, specifying the task type directly
/create-worker services/example-service/src/main/kotlin/io/miragon/example/adapter/process/NewsletterSubscriptionProcessApi.kt NEWSLETTER_SEND_WELCOME_MAIL

# From a BPMN file — skill finds the ProcessApi automatically
/create-worker services/example-service/src/main/resources/bpmn/newsletter.bpmn

# No arguments — skill searches for ProcessApi files and asks which task to generate
/create-worker
```

If no task type constant is provided, the skill lists all available `TaskTypes.*` constants and asks which one to use.

## What This Skill Creates or Updates

- One `@Component` `@JobWorker` class for the specified service task
- Located in `adapter/inbound/zeebe/`

If the file already exists, the skill opens it, compares it to the expected structure, and applies any missing or stale
parts.

## Pattern

```kotlin
@Component
class AbortRegistrationWorker(
    private val useCase: AbortSubscriptionUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.NEWSLETTER_ABORT_REGISTRATION)
    fun handle(@Variable subscriptionId: UUID) {
        log.debug { "Received job to abort registration for subscriptionId: $subscriptionId" }
        useCase.abort(SubscriptionId(subscriptionId))
    }
}
```

## Key Rules

- `@JobWorker(type = ...)` must reference `ProcessApi.TaskTypes.CONSTANT` — not a hardcoded string
- Inject the use-case interface as the only constructor parameter; never depend on the Camunda SDK in the constructor
- Use `@Variable` by default; switch to `@VariableAsType` only when a typed variables class already exists or many
  variables are needed
- Include a `KotlinLogging.logger {}` for debug-level tracing
- `@Variable`-annotated parameters must use **primitive types** (`UUID`, `String`); wrap them in domain value objects
  inside the `handle` body before delegating to the use case (e.g. `useCase.handle(NewsletterId(newsletterId))`)
- Before writing the worker, scan the `domain/` package for an existing value object matching the variable name.
  If found, use it with a conversion in the handle body. If not found, create the domain type first.

## Instructions

### Step 1 – Resolve the input source

Determine where the process constants come from based on `$ARGUMENTS`:

- **ProcessApi file (`.kt`)**: read it directly. Extract: package name, object name, all `TaskTypes.*` constants, and
  all `Variables.*` constants.
- **BPMN file (`.bpmn`)**: search the same service module for a `*ProcessApi.kt` file (Glob
  `**/adapter/process/*ProcessApi.kt`). If found, read it as above. If not found, ask the user whether to continue
  without type-safe constants.
- **No argument / plain description**: search the whole codebase for `*ProcessApi.kt` files (Glob
  `**/adapter/process/*ProcessApi.kt`). List them and ask the user which one to use.

### Step 2 – Determine the target package and source root

Derive the base package from the ProcessApi package (e.g. `io.miragon.example.adapter.process` → base:
`io.miragon.example`). Worker package: `<base>.adapter.inbound.zeebe`.

Determine the source root path from the ProcessApi file path (e.g. `services/example-service/src/main/kotlin/`).

### Step 3 – Determine the target TaskType

If a task type constant was provided in `$ARGUMENTS`, use it directly. Otherwise, list all constants from
`TaskTypes.*` and ask the user which one to generate a worker for.

Convert the constant name to PascalCase and append `Worker` to derive the class name
(e.g. `NEWSLETTER_ABORT_REGISTRATION` → `AbortRegistrationWorker`).

### Step 4 – Generate or update the worker

Check whether a file with the derived name already exists at the target location.

**If the file does not exist — generate:**

Follow the Pattern above. Concretely:

- `@Component` class with a constructor parameter for the use case (placeholder name and type)
- `@JobWorker(type = TaskTypes.THE_CONSTANT)` on the `handle` method
- `@Variable` for each process variable by default; `@VariableAsType` when a typed class already exists or many
  variables are needed — both from `io.camunda.client.annotation`
- `KotlinLogging.logger {}` for debug tracing
- Scan `**/domain/` for an existing value object matching each `Variables.*` constant (e.g. `NEWSLETTER_ID` →
  `NewsletterId`). `@Variable` parameters use primitive types (`UUID`, `String`); the body wraps them in domain types
  before delegating (e.g. `useCase.handle(DomainType(rawValue))`).
  **Never** leave a `TODO` for domain-type substitution — resolve the type now, creating it if necessary.

**If the file already exists — update:**

Read the existing file. Compare it against the expected structure. Apply only what is missing or outdated:
correct the `@JobWorker` type reference if stale, align variable injection with the ProcessApi `Variables.*` constants,
preserve any existing use-case delegation logic.

### Step 5 – Report

Report the file created, updated, or skipped (if nothing changed) and remind the developer to:

1. Add the worker to the Spring component scan (automatic if the package is within the base package)
2. Run `/test-worker` to generate a unit test for the worker
3. Call `/create-worker` again for any remaining service tasks
