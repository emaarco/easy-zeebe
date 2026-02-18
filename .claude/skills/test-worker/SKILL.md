---
name: test-worker
description: Generate a unit test for a Zeebe @JobWorker class following the project's mockk test patterns. Use when the user asks to write or generate tests for a job worker.
argument-hint: "<path-to-worker-file>"
allowed-tools: Read, Write, Glob, Bash(./gradlew *)
---

# Skill: test-worker

Generate a new unit test or update an existing one for a Zeebe job worker class.
This skill follows the project's established test patterns.

## What is a Job Worker?

A job worker is an **inbound adapter** in hexagonal terms.
It lives at the boundary where the process engine reaches into your application:
Zeebe polls your worker for jobs and, when one is available, invokes the `handle` method.
It's much like the engine making a request to your code to perform some work.

The worker translates the Zeebe job (variables, job key) into a domain call on a use-case interface.
After that it completes the job and returns control to the engine.

## Usage

```
/test-worker <path-to-worker-file>
```

Example:

```
/test-worker services/example-service/src/main/kotlin/io/miragon/example/adapter/inbound/zeebe/SendWelcomeMailWorker.kt
```

## Pattern

Worker unit tests follow this pattern
(see `AbortRegistrationWorkerTest.kt` and `SendWelcomeMailWorkerTest.kt`):

```kotlin
class WorkerNameTest {

    private val useCase = mockk<UseCaseInterface>()
    private val underTest = WorkerName(useCase)

    @Test
    fun `should perform action when job is received`() {

        // Given: a subscription and mocked service-calls
        val subscriptionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        every { useCase.method(SubscriptionId(subscriptionId)) } just Runs

        // When: the worker handles the job
        underTest.handle(subscriptionId)

        // Then: the use case is called with the correct subscription ID
        verify(exactly = 1) { useCase.method(SubscriptionId(subscriptionId)) }
        confirmVerified(useCase)
    }
}
```

## SDK Context

This project uses:

- **Production**: `io.camunda:camunda-spring-boot-starter` — annotations from `io.camunda.client.annotation` (
  `@JobWorker`, `@Variable`, `@VariableAsType`)
- **Testing**: `io.camunda:camunda-process-test-spring` — test utilities from `io.camunda.process.test.api` (used in
  process integration tests, not in worker unit tests)

Worker unit tests use `io.mockk` only — no Camunda test runtime is needed.
If you encounter imports or usage patterns not covered by this skill,
ask the user before proceeding how to handle it and whether to add it to the skill.

## Key Rules

- Use `mockk` / `io.mockk.*` for mocking (no Spring context required)
- The class under test is instantiated directly, not via Spring
- Use `confirmVerified(useCase)` to catch unexpected interactions
- One test method per distinct happy-path behavior
- If the worker has no return value, stub with `just Runs`; if it returns a value, use `returns <value>`
- By default, write only one test case; if multiple are needed, ask for permission first

## Instructions

1. **Read and identify** — read the worker file at `$ARGUMENTS` and extract:
    - Worker class name and the use-case interface injected via constructor
    - Variable injection style:
        - **`@Variable param: Type`** — injects one variable by name; the test calls `handle(value)` with the raw value
        - **`@VariableAsType variables: MyVarsClass`** — injects all variables as a typed object; the test instantiates
          the class and passes it, e.g. `handle(MyVarsClass(subscriptionId = subscriptionId))`
    - The `@JobWorker`-annotated `handle` method signature
    - The use-case method called inside `handle` and its parameter types
    - Domain wrapper types used (e.g. `SubscriptionId`)

2. **Locate the test file** — mirror `src/main/kotlin` → `src/test/kotlin`, same package path, append `Test` to the
   class name. If the file already exists, open it and switch to fix mode. If not, proceed to generate a new file.

3. **Determine required test cases** — normally one test per distinct happy-path behavior. If the worker calls the
   use case conditionally or has multiple code paths, list the cases and ask for permission before writing more than
   one.

4. **Generate or fix the test file**:
    - Package declaration matching the worker's package
    - Imports: `io.mockk.*`, `org.junit.jupiter.api.Test`, domain types
    - Instantiate the worker directly (no Spring context)
    - For each test: stub use case with exact values → call `handle(...)` → `verify(exactly = 1)` with exact values →
      `confirmVerified(useCase)`
    - Use fixed UUID strings (`123e4567-e89b-12d3-a456-426614174000`) for deterministic test data

5. **Write, run, and report** — write the file to the located path; run all tests in the class via an appropriate
   Gradle command; report the created or updated file path and a brief summary.
