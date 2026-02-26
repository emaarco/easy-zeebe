// ============================================================
// PROCESS TEST TEMPLATE
// Copy this file as a starting point; delete sections you don't need.
// Replace every <PLACEHOLDER> with project-specific values.
// ============================================================

package <PACKAGE>                         // e.g. io.miragon.example.adapter.process

import com.ninjasquad.springmockk.MockkBean
import io.camunda.client.CamundaClient
import io.camunda.client.api.response.ProcessInstanceEvent
import io.camunda.process.test.api.CamundaAssert
import io.camunda.process.test.api.CamundaProcessTestContext
import io.camunda.process.test.api.CamundaSpringProcessTest
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors
import io.miragon.common.test.config.TestProcessEngineConfiguration
import io.mockk.Called
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.Duration
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@CamundaSpringProcessTest
@Import(TestProcessEngineConfiguration::class)
class <ProcessName>ProcessTest {

    // ── Infrastructure ──────────────────────────────────────────────────────

    @Autowired
    private lateinit var camundaClient: CamundaClient           // needed only for startProcessAt helper

    @Autowired
    private lateinit var processTestContext: CamundaProcessTestContext  // needed only for timer tests

    @Autowired
    private lateinit var processPort: <ProcessAdapter>          // e.g. NewsletterSubscriptionProcessAdapter

    // ── Mocks (one @MockkBean per use-case injected by any worker) ──────────

    @MockkBean
    private lateinit var <useCase1>: <UseCase1Interface>        // e.g. sendConfirmationMailUseCase: SendConfirmationMailUseCase

    // @MockkBean                                               // repeat for every use-case
    // private lateinit var <useCase2>: <UseCase2Interface>

    // ── Setup ───────────────────────────────────────────────────────────────

    @BeforeEach
    fun setup() {
        every { <useCase1>.<method>(any()) } just Runs          // default: void method that must not throw
        // every { <useCase2>.<method>(any()) } just Runs
    }

    // ════════════════════════════════════════════════════════════════════════
    // HAPPY PATH
    // Drive the process via the process adapter from start to completion.
    // Use distinct UUIDs per test to avoid correlation conflicts.
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `happy path - <short description of main flow>`() {

        // given
        val id = UUID.fromString("<unique-uuid-1>")

        // when
        val instanceKey = processPort.<startMethod>(<DomainId>(id))
        // processPort.<sendMessageMethod>(...)    // include if a message completes the happy path

        // then
        val instance = ProcessInstanceSelectors.byKey(instanceKey)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        verify { <useCase1>.<method>(<DomainId>(id)) }
        // verify { <useCase2> wasNot Called }
        confirmVerified(<useCase1> /*, <useCase2>*/)
    }

    // ════════════════════════════════════════════════════════════════════════
    // MESSAGE PATH
    // Used when a receive task / intermediate message catch event drives a
    // branch of the process (e.g. user confirms via a second API call).
    // ════════════════════════════════════════════════════════════════════════

    /*
    @Test
    fun `message path - <description of message branch>`() {

        // given
        val id = UUID.fromString("<unique-uuid-2>")
        val instanceKey = processPort.<startMethod>(<DomainId>(id))

        // when - correlation message is sent
        processPort.<sendMessageMethod>(<DomainId>(id))

        // then
        val instance = ProcessInstanceSelectors.byKey(instanceKey)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        verify { <useCase1>.<method>(any()) }
        // verify { <useCase2> wasNot Called }
        confirmVerified(<useCase1> /*, <useCase2>*/)
    }
    */

    // ════════════════════════════════════════════════════════════════════════
    // TIMER PATH
    // Use processTestContext.increaseTime(...) to trigger timer events.
    // Start the process mid-flow with startProcessAt when you want to skip
    // earlier tasks and jump straight to the timer-guarded element.
    //
    // Duration unit: match the timer definition in the BPMN
    //   PT60S  → Duration.ofSeconds(60)
    //   PT2M   → Duration.ofMinutes(2)
    //   P1D    → Duration.ofDays(1)
    // ════════════════════════════════════════════════════════════════════════

    /*
    @Test
    fun `timer path - <description, e.g. registration expires after timeout>`() {

        // given - start directly at the element that has the timer attached
        val id = UUID.fromString("<unique-uuid-3>")
        val instance = startProcessAt(
            elementId = <ProcessApi>.Elements.<ELEMENT_WITH_TIMER>,
            variables  = mapOf(<ProcessApi>.Variables.<ID_VAR> to id.toString())
        )

        // when - advance time past the timer duration
        processTestContext.increaseTime(Duration.ofSeconds(<timer-seconds>))

        // then
        CamundaAssert.assertThatProcessInstance(instance)
            .hasCompletedElement(<ProcessApi>.Elements.<TASK_AFTER_TIMER>, 1)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        verify { <useCase1>.<method>(<DomainId>(id)) }
        // verify { <useCase2> wasNot Called }
        confirmVerified(<useCase1> /*, <useCase2>*/)
    }
    */

    // ════════════════════════════════════════════════════════════════════════
    // MID-PROCESS SCENARIO (startProcessAt helper)
    // Bypasses the start event and earlier tasks; useful to isolate a specific
    // segment of the process (e.g. a loop body, a boundary event, a late branch).
    // Only include this helper when at least one test uses startProcessAt.
    // ════════════════════════════════════════════════════════════════════════

    /*
    @Test
    fun `<mid-process scenario description>`() {

        // given - jump to a specific element
        val id = UUID.fromString("<unique-uuid-4>")
        val instance = startProcessAt(
            elementId = <ProcessApi>.Elements.<TARGET_ELEMENT>,
            variables  = mapOf(<ProcessApi>.Variables.<ID_VAR> to id.toString())
        )

        // when / then
        // ...
    }
    */

    // ── Private helper ───────────────────────────────────────────────────────
    // Remove this helper if no test uses startProcessAt.

    /*
    private fun startProcessAt(elementId: String, variables: Map<String, Any>): ProcessInstanceEvent {
        return camundaClient.newCreateInstanceCommand()
            .bpmnProcessId(<ProcessApi>.PROCESS_ID)
            .latestVersion()
            .variables(variables)
            .startBeforeElement(elementId)
            .send()
            .join()
    }
    */
}
