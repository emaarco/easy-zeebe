---
name: create-rest-controller
description: Scaffold or update a REST controller (inbound adapter). Accepts a use-case port file, a BPMN model, or a plain description. Use when the user wants to generate an inbound REST adapter for a use case.
argument-hint: "[<path-to-UseCase-port-file>] [http-verb] [path]"
allowed-tools: Read, Write, Glob
---

# Skill: create-rest-controller

Generate one REST controller (inbound adapter) for a specific use case.
To generate controllers for multiple use cases, invoke this skill once per use case.

## Usage

```
/create-rest-controller [<path-to-UseCase-port-file>] [http-verb] [path]
```

Examples:

```
# From a use-case port file, specifying verb and path
/create-rest-controller services/example-service/src/main/kotlin/io/miragon/example/application/port/inbound/SubscribeToNewsletterUseCase.kt POST /api/subscriptions/subscribe

# From a use-case port file — skill asks for verb and path if not supplied
/create-rest-controller services/example-service/src/main/kotlin/io/miragon/example/application/port/inbound/ConfirmSubscriptionUseCase.kt

# No arguments — skill searches for use-case port files and asks which one to use
/create-rest-controller
```

If the HTTP verb or path are not provided, the skill infers sensible defaults from the use-case name
and asks for confirmation before generating.

## What This Skill Creates or Updates

- One `@RestController` class for the specified use case
- Located in `adapter/inbound/rest/`
- Nested request and response `data class` DTOs
- A private `toCommand()` extension function for request-to-command mapping

If the file already exists, the skill opens it, compares it to the expected structure, and applies any
missing or stale parts.

## Pattern

```kotlin
@RestController
@RequestMapping("/api/subscriptions")
class SubscribeToNewsletterController(private val useCase: SubscribeToNewsletterUseCase) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/subscribe")
    fun subscribeToNewsletter(@RequestBody input: SubscriptionForm): ResponseEntity<Response> {
        log.debug { "Received REST-request to subscribe to newsletter: $input" }
        val subscriptionId = useCase.subscribe(input.toCommand())
        return ResponseEntity.ok().body(Response(subscriptionId.value.toString()))
    }

    data class SubscriptionForm(
        val email: String,
        val name: String,
        val newsletterId: String
    )

    data class Response(val subscriptionId: String)

    private fun SubscriptionForm.toCommand() = SubscribeToNewsletterUseCase.Command(
        Email(email),
        Name(name),
        NewsletterId(UUID.fromString(newsletterId))
    )
}
```

For void use cases (no return value), the pattern uses `@PathVariable` and returns `ResponseEntity<Void>`:

```kotlin
@PostMapping("/confirm/{subscriptionId}")
fun confirmSubscription(@PathVariable subscriptionId: String): ResponseEntity<Void> {
    log.debug { "Received REST-request to confirm subscription: $subscriptionId" }
    useCase.confirm(SubscriptionId(UUID.fromString(subscriptionId)))
    return ResponseEntity.ok().build()
}
```

## Key Rules

- One controller class per use case; one endpoint method per controller is the default
- Inject the use-case interface as the **only** constructor parameter
- Wrap all request fields in domain value objects inside `toCommand()` before passing to the use case;
  never pass raw `String` or `UUID` directly to the use case method
- Always return `ResponseEntity<T>`: use `ResponseEntity<Response>` when the use case returns a value,
  `ResponseEntity<Void>` when it returns `Unit`
- Request/response DTOs are `data class` types nested inside the controller class
- Log at `debug` level on entry; include the input in the log message; never log sensitive data
- Before writing the controller, scan the `domain/` package for value objects matching each request field.
  Use them in `toCommand()` directly. If a needed value object does not exist, create it first.

## Instructions

### Step 1 – Resolve the input source

Determine which use-case interface to target based on `$ARGUMENTS`:

- **Use-case port file (`.kt`)**: read it directly. Extract: package name, interface name, method signature(s),
  `Command` nested class fields (if present), return type.
- **BPMN file (`.bpmn`)**: search the same service module for use-case port files
  (Glob `**/application/port/inbound/*UseCase.kt`). List them and ask which one to target.
- **No argument / plain description**: search the whole codebase for use-case port files
  (Glob `**/application/port/inbound/*UseCase.kt`). List them and ask which one to use.

### Step 2 – Determine the target package and source root

Derive the base package from the use-case port package
(e.g. `io.miragon.example.application.port.inbound` → base: `io.miragon.example`).
Controller package: `<base>.adapter.inbound.rest`.

Determine the source root path from the port file path
(e.g. `services/example-service/src/main/kotlin/`).

### Step 3 – Determine the HTTP verb and path

If `$ARGUMENTS` contains a verb and path, use them.

Otherwise, infer defaults from the use-case name:

- Name starts with `Create`, `Subscribe`, `Register`, `Add` → `POST`
- Name starts with `Update`, `Confirm`, `Change` → `POST` (or `PUT` — ask)
- Name starts with `Delete`, `Remove`, `Cancel`, `Abort` → `DELETE` (or `POST` — ask)
- Name starts with `Get`, `Find`, `Search`, `List` → `GET`

Derive a sensible URL path from the use-case name and the domain noun (e.g.
`SubscribeToNewsletterUseCase` → `POST /api/subscriptions/subscribe`).

Ask the user to confirm or adjust the verb and path before generating.

### Step 4 – Generate or update the controller

Derive the controller class name from the use-case interface name by replacing `UseCase` with `Controller`
(e.g. `SubscribeToNewsletterUseCase` → `SubscribeToNewsletterController`).

Check whether a file with that name already exists at the target location.

**If the file does not exist — generate:**

Follow the Pattern above. Concretely:

- `@RestController` + `@RequestMapping("<base-path>")` at class level
- Constructor parameter: the use-case interface
- One method annotated with `@PostMapping`/`@GetMapping`/etc.
    - `@RequestBody input: <RequestForm>` for POST/PUT requests with a body
    - `@PathVariable id: String` for path-parameter-only endpoints (e.g. confirm, delete)
    - `@RequestParam` for query parameters (e.g. search/list endpoints)
- Nested `data class <Name>Form(...)` for request fields (one `String` field per `Command` field)
- Nested `data class Response(...)` for the return value (one `String` field per domain ID/value;
  omit if the use case returns `Unit`)
- Private extension function `<Name>Form.toCommand()` wrapping each field in its domain value object
- `KotlinLogging.logger {}` for debug-level entry logging
- Scan `**/domain/` for existing value objects matching each field; use them in `toCommand()`.
  **Never** leave a `TODO` for domain-type substitution — resolve the type now, creating it if necessary.

**If the file already exists — update:**

Read the existing file. Compare it against the use-case interface signature. Apply only what is missing
or outdated: add missing fields to the request form, update the return type, correct the `toCommand()`
mapping. Preserve any existing logic.

### Step 5 – Report

Report the file created, updated, or skipped (if nothing changed) and remind the developer to:

1. Verify the base path in `@RequestMapping` is consistent with the existing API structure
2. Run `/test-rest-adapter` to generate a unit test for the controller
3. Call `/create-rest-controller` again for any remaining use cases