---
name: test-rest-adapter
description: Generate or fix a test for a REST controller (inbound adapter) using @WebMvcTest and MockMvc. Use when the user asks to write or generate tests for a REST controller.
argument-hint: "<path-to-controller-file>"
allowed-tools: Read, Write, Edit, Glob, Bash(./gradlew *)
---

# Skill: test-rest-adapter

Generate or fix a test for a REST controller using `@WebMvcTest` with `MockMvc` and `@MockkBean`.

## Usage

```
/test-rest-adapter <path-to-controller-file>
```

Example:

```
/test-rest-adapter services/example-service/src/main/kotlin/io/miragon/example/adapter/inbound/rest/SubscribeToNewsletterController.kt
```

## Pattern

`@WebMvcTest` loads only the web layer (no full Spring context, no JPA, no Zeebe).
Use-case dependencies are replaced with `@MockkBean` from `com.ninjasquad.springmockk`.

```kotlin
@WebMvcTest(SomeController::class)
class SomeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var useCase: SomeUseCase

    private val mapper = ObjectMapper()

    @Test
    fun `user performs business action`() {                 // ← business-oriented name

        // given: valid input data & rest-operation
        val input = mapOf("field" to "value")
        val returnedId = SomeId("123e4567-e89b-12d3-a456-426614174000")
        val expectedCommand = SomeUseCase.Command(SomeField("value"))
        every { useCase.method(any()) } returns returnedId  // stub with any()

        val operation = post("/api/some-path")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(input))

        // when: request is performed
        val response = mockMvc.perform(operation).andReturn()

        // then: assert that use-case was called & response is correct
        val expectedResponse = mapOf("id" to returnedId.value)
        assertThat(response.response.status).isEqualTo(200)
        assertThat(response.response.contentAsString).isEqualTo(mapper.writeValueAsString(expectedResponse))
        verify { useCase.method(expectedCommand) }          // verify exact command
        confirmVerified(useCase)
    }

    @Test
    fun `user performs void business action`() {

        // given: valid path variable & rest-operation
        val pathId = "123e4567-e89b-12d3-a456-426614174000"
        val operation = post("/api/some-path/{id}", pathId)
        every { useCase.voidMethod(any()) } just Runs

        // when: request is performed
        val response = mockMvc.perform(operation).andReturn()

        // then: assert that use-case was called & response is 200
        assertThat(response.response.status).isEqualTo(200)
        verify { useCase.voidMethod(SomeId(pathId)) }
        confirmVerified(useCase)
    }
}
```

## Key Rules

- `@WebMvcTest(ControllerClass::class)` — web slice only; no JPA, no Zeebe
- `@MockkBean` for every use-case / adapter dependency
- `private val mapper = ObjectMapper()` — serialize request bodies via ObjectMapper, not inline strings
- **Test method names are business-oriented**: describe what the user/use-case achieves
  (e.g. `` `user subscribes to newsletter` ``), never HTTP mechanics
- Structure each test with `// given: …`, `// when: …`, `// then: …` comment sections with brief description
- Stub use-case with `any()` in `every { … }`, then verify the **exact command object** in `verify { … }`
- Use `andReturn()` and assert on `response.response.status` and `response.response.contentAsString` with AssertJ
- `confirmVerified(useCase)` — detect unexpected calls
- Value-type IDs with a `String` secondary constructor (e.g. `SubscriptionId("uuid-string")`) can be used directly
- One test per endpoint — happy path only unless error behaviour is explicit in the controller

## Instructions

1. **Read and identify** — read the controller file at `$ARGUMENTS` and extract:
    - Controller class name, base `@RequestMapping` path, and each endpoint (`@GetMapping`, `@PostMapping`, etc.)
    - For each endpoint: HTTP method, sub-path, `@RequestBody` / `@PathVariable` / `@RequestParam` parameters,
      return type
    - Injected use-case / adapter dependencies (constructor parameters)

2. **Locate the test file** — mirror `src/main/kotlin` → `src/test/kotlin`, same package path, append `Test` to the
   class name. If the file already exists, open it and switch to fix mode. If not, proceed to generate a new file.

3. **Determine required test cases** — one test per endpoint, happy-path only. If error behaviour is explicit in the
   controller (e.g. a specific status code on failure), list the additional case and ask for permission before writing
   more than one test per endpoint.

4. **Generate or fix the test file**:
    - `@WebMvcTest(ControllerClass::class)`, `@Autowired MockMvc`, `@MockkBean` for every dependency,
      `private val mapper = ObjectMapper()`
    - For each endpoint test:
        - POST with body → build `expectedCommand`, stub with `any()`, perform POST with
          `mapper.writeValueAsString(input)`, `andReturn()`, assert status + `contentAsString`, `verify` exact command
        - POST/DELETE with path variable → stub with `any()` + `just Runs`, perform request, `andReturn()`,
          assert status 200, `verify` exact id object
        - GET → stub return value, perform GET, `andReturn()`, assert status 200 + `contentAsString`
    - Structure each test with `// given:`, `// when:`, `// then:` comment lines
    - End every test with `confirmVerified(useCase)`
    - Use fixed UUID strings (`123e4567-e89b-12d3-a456-426614174000`) for deterministic test data

5. **Write, run, and report** — write the file to the located path; run all tests in the class via an appropriate
   Gradle command; report the created or updated file path and a brief summary.
