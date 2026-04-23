package io.miragon.example.adapter.process

import com.ninjasquad.springmockk.MockkBean
import io.camunda.client.CamundaClient
import io.camunda.client.api.response.ProcessInstanceEvent
import io.camunda.process.test.api.CamundaAssert
import io.camunda.process.test.api.CamundaProcessTestContext
import io.camunda.process.test.api.CamundaSpringProcessTest
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors
import io.miragon.common.test.config.TestProcessEngineConfiguration
import io.miragon.example.adapter.outbound.zeebe.MembershipProcessAdapter
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.Elements
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.Variables
import io.miragon.example.application.port.inbound.ClaimMembershipUseCase
import io.miragon.example.application.port.inbound.SendConfirmationMailUseCase
import io.miragon.example.application.port.inbound.SendRejectionMailUseCase
import io.miragon.example.application.port.inbound.SendWelcomeMailUseCase
import io.miragon.example.domain.MembershipId
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
import java.util.*

/**
 * Process test for the MiraVelo Membership Process.
 * Uses native Camunda 8.8 test API with @CamundaSpringProcessTest and Spring Boot for component injection.
 * Workers are automatically registered via @JobWorker annotation.
 * Uses H2 an in-memory database for testing (configured in test/resources/application.yaml).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@CamundaSpringProcessTest
@Import(TestProcessEngineConfiguration::class)
class MiraveloMembershipProcessTest {

    @Autowired
    private lateinit var camundaClient: CamundaClient

    @Autowired
    private lateinit var processTestContext: CamundaProcessTestContext

    @Autowired
    private lateinit var processPort: MembershipProcessAdapter

    @MockkBean
    private lateinit var claimMembershipUseCase: ClaimMembershipUseCase

    @MockkBean
    private lateinit var sendConfirmationMailUseCase: SendConfirmationMailUseCase

    @MockkBean
    private lateinit var sendWelcomeMailUseCase: SendWelcomeMailUseCase

    @MockkBean
    private lateinit var sendRejectionMailUseCase: SendRejectionMailUseCase

    @BeforeEach
    fun setup() {
        every { claimMembershipUseCase.claim(any<MembershipId>()) } returns true
        every { sendConfirmationMailUseCase.sendConfirmationMail(any<MembershipId>()) } just Runs
        every { sendWelcomeMailUseCase.sendWelcomeMail(any<MembershipId>()) } just Runs
        every { sendRejectionMailUseCase.sendRejectionMail(any<MembershipId>()) } just Runs
    }

    @Test
    fun `happy path - capacity available, user confirms, receives welcome mail`() {

        // given
        val membershipId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b86")
        every { claimMembershipUseCase.claim(MembershipId(membershipId)) } returns true

        // when - start process and immediately confirm membership
        val instanceKey = processPort.registerMembership(MembershipId(membershipId))
        processPort.confirmMembership(MembershipId(membershipId))

        // then - process should complete successfully with welcome mail sent
        val instance = ProcessInstanceSelectors.byKey(instanceKey)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        verify { claimMembershipUseCase.claim(MembershipId(membershipId)) }
        verify { sendConfirmationMailUseCase.sendConfirmationMail(MembershipId(membershipId)) }
        verify { sendWelcomeMailUseCase.sendWelcomeMail(MembershipId(membershipId)) }
        verify { sendRejectionMailUseCase wasNot Called }
        confirmVerified(claimMembershipUseCase, sendConfirmationMailUseCase, sendWelcomeMailUseCase, sendRejectionMailUseCase)
    }

    @Test
    fun `rejection path - no capacity available, rejection mail is sent`() {

        // given
        val membershipId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b87")
        every { claimMembershipUseCase.claim(MembershipId(membershipId)) } returns false

        // when - start process with no capacity
        val instance = startProcessAt(
            elementId = Elements.SERVICE_TASK_CLAIM_MEMBERSHIP,
            membershipId = MembershipId(membershipId),
            extraVariables = mapOf(Variables.HAS_EMPTY_SPOTS to false)
        )

        // then - rejection mail is sent and process ends rejected
        CamundaAssert.assertThatProcessInstance(instance)
            .hasCompletedElement(Elements.SERVICE_TASK_SEND_REJECTION_MAIL, 1)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        verify { sendRejectionMailUseCase.sendRejectionMail(MembershipId(membershipId)) }
        verify { sendConfirmationMailUseCase wasNot Called }
        verify { sendWelcomeMailUseCase wasNot Called }
        confirmVerified(claimMembershipUseCase, sendConfirmationMailUseCase, sendWelcomeMailUseCase, sendRejectionMailUseCase)
    }

    private fun startProcessAt(
        elementId: String,
        membershipId: MembershipId,
        extraVariables: Map<String, Any> = emptyMap(),
    ): ProcessInstanceEvent {
        val variables = mutableMapOf<String, Any>(Variables.MEMBERSHIP_ID to membershipId.value.toString())
        variables.putAll(extraVariables)
        return camundaClient.newCreateInstanceCommand()
            .bpmnProcessId(MiraveloMembershipProcessApi.PROCESS_ID)
            .latestVersion()
            .variables(variables)
            .startBeforeElement(elementId)
            .send()
            .join()
    }
}
