---
name: test-application-service
description: Generate a unit test for an application service (use case) following the project's mockk test patterns. Use when the user asks to write or generate tests for an application service.
argument-hint: "<path-to-service-file>"
allowed-tools: Read, Write, Glob, Bash(./gradlew *)
---

# Skill: test-application-service

Generate a unit test for an application service (use case) following the project's established test patterns.

## Usage

```
/test-application-service <path-to-service-file>
```

Example:

```
/test-application-service services/example-service/src/main/kotlin/io/miragon/example/application/service/SendConfirmationMailService.kt
```

## Pattern

Application service tests are pure unit tests using mockk. No Spring context required.
See `SendConfirmationMailServiceTest.kt` and `AbortSubscriptionServiceTest.kt` as reference implementations.

```kotlin
class ServiceNameTest {

    private val repository = mockk<RepositoryInterface>()
    private val underTest = ServiceName(repository = repository)

    @Test
    fun `describe what the service does`() {
        val id = SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val subscription = testNewsletterSubscription(id = id)
        every { repository.find(id) } returns subscription

        underTest.execute(id)

        verify { repository.find(id) }
        confirmVerified(repository)
    }
}
```

## Key Rules

- Instantiate the service directly — no Spring context
- `mockk<Interface>()` for each constructor dependency
- `confirmVerified(mock)` inline at the end of **each** test — not in `@AfterEach`
- Stub Unit-returning methods with `just Runs`; use `returns value` otherwise
- One `@Test` per distinct behaviour (happy path + exception if the service throws)
- Use test builders from `domain/TestObjectBuilder.kt` (e.g. `testNewsletterSubscription()`)
- Fixed UUID strings for deterministic test data
- Backtick test names

## Instructions

1. **Read and identify** — read the service file at `$ARGUMENTS` and extract:
    - Service class name and the use-case interface it implements
    - Constructor dependencies (repository interfaces, outbound ports)
    - Each public method: signature, what it calls, what it returns or throws
    - Domain value types used (e.g. `SubscriptionId`, `NewsletterSubscription`)

   Also locate test builders — look for `test*()` functions in `domain/TestObjectBuilder.kt`. If none
   exists, ask before generating test data manually.

2. **Locate the test file** — mirror `src/main/kotlin` → `src/test/kotlin`, same package path, append `Test` to the
   class name. If the file already exists, open it and switch to fix mode. If not, proceed to generate a new file.

3. **Determine required test cases** — one happy-path test per public method; add an exception test only if
   the service explicitly throws (e.g. `NoSuchElementException`). Keep to ≤3 tests; if more scenarios
   exist, list them and ask for permission before writing.

4. **Generate or fix the test file**:
    - Instantiate the service directly (no Spring context); `mockk<Interface>()` per constructor dependency
    - Stub Unit-returning methods with `just Runs`; use `returns value` otherwise
    - `verify { ... }` for each interaction; `confirmVerified(mock)` at the end of every test
    - Use test builders (e.g. `testNewsletterSubscription()`) and fixed UUID strings for test data
    - Backtick test names describing what the service does

5. **Write, run, and report** — write the file to the located path; run all tests in the class via an
   appropriate Gradle command; report the created or updated file path and a brief summary.
