---
name: test-process-adapter
description: Generate a unit test for a Zeebe process out-adapter following the project's mockk test patterns. Use when the user asks to write or generate tests for a process adapter.
argument-hint: "<path-to-adapter-file>"
allowed-tools: Read, Write, Glob, Bash(./gradlew *)
---

# Skill: test-process-adapter

Generate a unit test for a Zeebe process out-adapter following the project's established test patterns.

## What is a Process Adapter?

A process adapter is an **outbound adapter** in hexagonal terms.
It lives at the boundary where *your application*reaches out to the process engine:
your domain or application service calls the adapter to initiate or advance a process instance —
start a process, send a correlation message, or query state.

This is the mirror of a job worker:

| Direction    | Pattern         | Who initiates the call?                     |
|--------------|-----------------|---------------------------------------------|
| **Inbound**  | Job Worker      | The engine calls your code (or you poll it) |
| **Outbound** | Process Adapter | Your code calls the engine                  |

The process adapter collects all outgoing engine operations for one process in a single class,
so the rest of your codebase depends only on a clean port interface — never on the Camunda SDK directly.

## Usage

```
/test-process-adapter <path-to-adapter-file>
```

Example:

```
/test-process-adapter services/example-service/src/main/kotlin/io/miragon/example/adapter/outbound/zeebe/NewsletterSubscriptionProcessAdapter.kt
```

## SDK Context

This project uses:

- **Production**: `io.camunda:camunda-spring-boot-starter`
- **Wrapper** — `ProcessEngineApi` from `io.miragon.common.zeebe.engine`
- **Testing**: adapter unit tests use `io.mockk` only; no Camunda test runtime needed

If you encounter imports or usage patterns not covered by this skill,
ask the user before proceeding how to handle it and whether to add it to the skill.

## Reference Pattern

See `NewsletterSubscriptionProcessAdapterTest.kt` for the canonical example. The structure is:

- Mock `ProcessEngineApi` with `mockk`
- Instantiate the adapter directly (no Spring context)
- One `@Test` per public method:
    - For `startProcess` calls: verify `processId` and `variables` using ProcessApi constants; assert the returned key
    - For `sendMessage` calls: verify `messageName`, `correlationId`, and `variables` using ProcessApi constants
- All string values (processId, messageName, variable keys) come from the ProcessApi object — never raw literals
- End each test with `confirmVerified(engineApi)` to catch unexpected interactions

## Key Rules

- Use `io.mockk.*` for mocking `ProcessEngineApi`
- Use `org.assertj.core.api.Assertions.assertThat` for return-value assertions
- Use `confirmVerified(engineApi)` at the end of every test to catch unexpected calls
- Import the exact ProcessApi class used in the adapter under test
- All string constants (processId, messageName, variable keys) must come from the ProcessApi object — no raw literals

## Instructions

1. **Read and identify** — read the adapter file at `$ARGUMENTS` and extract:
    - Adapter class name and the outbound port interface it implements (if any)
    - The ProcessApi object imported (e.g. `NewsletterSubscriptionProcessApi`)
    - Each public method: name, parameters, return type
    - Which `ProcessEngineApi` methods are called (`startProcess`, `sendMessage`, etc.)
    - Which ProcessApi constants are referenced (PROCESS_ID, Messages.*, Variables.*, etc.)

   Then read the ProcessApi file to confirm the exact constant names and nested object structure.

2. **Locate the test file** — mirror `src/main/kotlin` → `src/test/kotlin`, same package path, append `Test` to the
   class name. If the file already exists, open it and switch to fix mode. If not, proceed to generate a new file.

3. **Determine required test cases** — one test per public adapter method. If a method has multiple code paths, list
   them and ask for permission before writing more than one test for that method.

4. **Generate or fix the test file**:
    - Package declaration matching the adapter's package
    - Imports: `io.mockk.*`, `org.assertj.core.api.Assertions.assertThat`, `org.junit.jupiter.api.Test`,
      `ProcessEngineApi`, ProcessApi, domain types
    - Mock `ProcessEngineApi`, instantiate the adapter directly
    - Use ProcessApi constants for all string values — no raw string literals
    - `assertThat` for return values; `verify { ... }` with named args; `confirmVerified(engineApi)` at end of each test
    - Use fixed UUID strings (`123e4567-e89b-12d3-a456-426614174000`) for deterministic test data

5. **Write, run, and report** — write the file to the located path; run all tests in the class via an appropriate
   Gradle command; report the created or updated file path and a brief summary.
