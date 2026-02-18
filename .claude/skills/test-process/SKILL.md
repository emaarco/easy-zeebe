---
name: test-process
description: Generate process integration tests for a Zeebe BPMN process using @CamundaSpringProcessTest. Use when the user asks to write integration tests for a Zeebe process.
argument-hint: "<process-name-or-bpmn-path>"
allowed-tools: Read, Write, Glob, Grep, Bash(./gradlew *)
---

# Skill: test-process

Generate process integration tests for a Zeebe BPMN process.

## Usage

```
/test-process <process-name-or-bpmn-path>
```

Example:
```
/test-process newsletter
/test-process services/example-service/src/main/resources/bpmn/newsletter.bpmn
```

## SDK Context

This project uses:

- **Test runtime**: `io.camunda:camunda-process-test-spring` — `@CamundaSpringProcessTest`, `CamundaAssert`,
  `CamundaProcessTestContext`, `ProcessInstanceSelectors` from `io.camunda.process.test.api`
- **Mocking**: `com.ninjasquad.springmockk.MockkBean` (Spring-aware mockk bean replacement)
- **Engine wrapper**: `TestProcessEngineConfiguration` from `io.miragon.common.test.config`

If you encounter imports or usage patterns not covered by this skill, ask the user before proceeding.

## Key Rules

- One `@Test` method per distinct process path (happy path, each timer expiry, each message branch, each boundary event)
- Use `@MockkBean` for every use-case interface that a worker injects — these are the collaborators
- Set up all mocks in `@BeforeEach` with `every { useCase.method(any()) } just Runs`
- Use fixed UUID strings for deterministic, reproducible test data — use different UUIDs per test to avoid correlation conflicts
- Drive the process via the process adapter (`processPort.*`) not via `camundaClient` directly, except when using `startBeforeElement` for mid-process scenarios
- Use `ProcessInstanceSelectors.byKey(instanceKey)` to create the selector for assertions
- Always end each test with `confirmVerified(...)` across all mocked use cases to catch unexpected interactions
- Use `verify { useCase wasNot Called }` for negative assertions (not `verify(exactly = 0) { ... }`)

## Instructions

### Step 1 – Locate and read the BPMN file

If `$ARGUMENTS` is a file path, read it directly. If it is a process name, search for `*$ARGUMENTS*.bpmn` in
`src/main/resources/bpmn/`. Read the BPMN and extract:

- Process ID (`<bpmn:process id="...">`)
- All service task types (`zeebe:taskDefinition type` attribute)
- All message names (`<bpmn:message name="...">`)
- All timer definitions and the boundary events they attach to
- All gateway branches and their conditions
- Element IDs for key tasks and events

### Step 2 – Read the ProcessApi file

Locate the ProcessApi file (typically `adapter/process/*ProcessApi.kt` in the same module). Read and extract:

- `PROCESS_ID`, `TaskTypes.*`, `Messages.*`, `Variables.*`, `Elements.*` constants

### Step 3 – Read the process adapter

Locate the process out-adapter (`adapter/outbound/zeebe/*ProcessAdapter.kt`). Identify:

- The class name (used for `@Autowired processPort`)
- The method names that start processes and send messages

### Step 4 – Read the workers

Locate all worker files in `adapter/inbound/zeebe/`. For each worker, identify:

- The injected use-case interface (constructor parameter) → needs a `@MockkBean`
- The `@Variable` parameters used

### Step 5 – Locate `TestProcessEngineConfiguration`

Search for `TestProcessEngineConfiguration.kt` in the test source tree to confirm its fully-qualified class name
for the `@Import` annotation.

### Step 6 – Identify process paths

Enumerate all distinct paths through the process:

1. **Happy path** — the primary successful flow
2. **Timer paths** — one per timer boundary event or intermediate timer catch event (time is advanced via
   `processTestContext.increaseTime(...)`)
3. **Message paths** — branches triggered by different correlation messages
4. **Error/compensation paths** — boundary events, abort flows, etc.

For mid-process scenarios (e.g. testing a path that starts mid-flow), use `camundaClient.newCreateInstanceCommand()`
with `.startBeforeElement(elementId)` and explicit variables — see the `startProcessAt` helper pattern below.

### Step 7 – Generate the test class

Test class structure:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@CamundaSpringProcessTest
@Import(TestProcessEngineConfiguration::class)
class <ProcessName>ProcessTest {

    @Autowired
    private lateinit var camundaClient: CamundaClient

    @Autowired
    private lateinit var processTestContext: CamundaProcessTestContext

    @Autowired
    private lateinit var processPort: <ProcessAdapter>

    // One @MockkBean per use-case interface injected by any worker
    @MockkBean
    private lateinit var someUseCase: SomeUseCase

    @BeforeEach
    fun setup() {
        every { someUseCase.method(any()) } just Runs
        // ... repeat for every mocked use case
    }

    @Test
    fun `happy path - <description>`() {
        // given
        val subscriptionId = UUID.fromString("...")

        // when
        val instanceKey = processPort.startMethod(...)
        processPort.sendMessageMethod(...)     // if applicable

        // then
        val instance = ProcessInstanceSelectors.byKey(instanceKey)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        verify { someUseCase.method(...) }
        verify { otherUseCase wasNot Called }
        confirmVerified(someUseCase, otherUseCase)
    }

    @Test
    fun `timer path - <description>`() {
        // given
        val subscriptionId = UUID.fromString("...")
        val instance = startProcessAt(
            elementId = ProcessApi.Elements.SOME_ELEMENT,
            subscriptionId = SubscriptionId(subscriptionId)
        )

        // when
        processTestContext.increaseTime(Duration.ofSeconds(<timer-seconds>))

        // then
        CamundaAssert.assertThatProcessInstance(instance)
            .hasCompletedElement(ProcessApi.Elements.TIMER_TASK_ELEMENT, 1)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        verify { someUseCase.method(...) }
        confirmVerified(someUseCase, otherUseCase)
    }

    // Private helper for mid-process scenarios
    private fun startProcessAt(elementId: String, subscriptionId: SubscriptionId): ProcessInstanceEvent {
        val variables = mapOf(ProcessApi.Variables.SUBSCRIPTION_ID to subscriptionId.value.toString())
        return camundaClient.newCreateInstanceCommand()
            .bpmnProcessId(ProcessApi.PROCESS_ID)
            .latestVersion()
            .variables(variables)
            .startBeforeElement(elementId)
            .send()
            .join()
    }
}
```

Adapt the template: remove `startProcessAt` if not needed, add timer tests only when the BPMN has timers, etc.

### Step 8 – Determine the test file location

Mirror the source layout:

- Source module root: derive from the BPMN file path (e.g. `services/example-service/`)
- Test file: `src/test/kotlin/<package-path>/<ProcessName>ProcessTest.kt`
- Package: same as the process adapter package (e.g. `io.miragon.example.adapter.process`)

Skip generating if a file with that name already exists.

### Step 9 – Write the test file

Write the generated class to the determined location.

### Step 10 – Verify

Run the generated tests using an appropriate Gradle command. Derive the module from the file path:

```
./gradlew :<module-path>:test --tests "*<ProcessName>ProcessTest*"
```

### Step 11 – Report

Report the created file path and a brief summary of which paths are covered by the generated tests.