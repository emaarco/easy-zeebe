package io.miragon.example.adapter.process

import com.ninjasquad.springmockk.MockkBean
import io.camunda.client.CamundaClient
import io.camunda.process.test.api.CamundaAssert
import io.camunda.process.test.api.CamundaProcessTestContext
import io.camunda.process.test.api.CamundaSpringProcessTest
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors
import io.miragon.common.test.assertions.hasCompletedElements
import io.miragon.common.test.config.TestProcessEngineConfiguration
import io.miragon.example.adapter.outbound.zeebe.MembershipProcessAdapter
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.Elements
import io.miragon.example.application.port.inbound.ClaimMembershipUseCase
import io.miragon.example.application.port.inbound.ReSendConfirmationMailUseCase
import io.miragon.example.application.port.inbound.RevokeClaimUseCase
import io.miragon.example.application.port.inbound.RevokeMembershipRequestUseCase
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
import org.awaitility.Awaitility
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.Duration
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

    @MockkBean
    private lateinit var reSendConfirmationMailUseCase: ReSendConfirmationMailUseCase

    @MockkBean
    private lateinit var revokeMembershipRequestUseCase: RevokeMembershipRequestUseCase

    @MockkBean
    private lateinit var revokeClaimUseCase: RevokeClaimUseCase

    @BeforeEach
    fun setup() {
        every { claimMembershipUseCase.claim(any<MembershipId>()) } returns true
        every { sendConfirmationMailUseCase.sendConfirmationMail(any<MembershipId>()) } just Runs
        every { sendWelcomeMailUseCase.sendWelcomeMail(any<MembershipId>()) } just Runs
        every { sendRejectionMailUseCase.sendRejectionMail(any<MembershipId>()) } just Runs
        every { reSendConfirmationMailUseCase.reSendConfirmationMail(any<MembershipId>()) } just Runs
        every { revokeMembershipRequestUseCase.revokeMembershipRequest(any<MembershipId>()) } just Runs
        every { revokeClaimUseCase.revokeClaim(any<MembershipId>()) } just Runs
    }

    @Test
    fun `happy path - capacity available, user confirms, membership is activated`() {

        // given
        val membershipId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b86")
        every { claimMembershipUseCase.claim(MembershipId(membershipId)) } returns true

        // when - start process, wait until user task is available, then complete it directly
        // via the process test context (the adapter's user-task lookup relies on variable-indexed
        // secondary storage which is eventually consistent inside the in-memory test container).
        processPort.registerMembership(MembershipId(membershipId))
        val instanceKey = awaitProcessInstance(membershipId)
        awaitUserTaskCreated(instanceKey)
        processTestContext.completeUserTask(Elements.USER_TASK_CONFIRM_MEMBERSHIP.value)

        // then - process completes, welcome mail is sent, activation signal thrown
        val instance = ProcessInstanceSelectors.byKey(instanceKey)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        CamundaAssert.assertThatProcessInstance(instance)
            .hasCompletedElements(
                Elements.SERVICE_TASK_CLAIM_MEMBERSHIP,
                Elements.SERVICE_TASK_SEND_CONFIRMATION_MAIL,
                Elements.USER_TASK_CONFIRM_MEMBERSHIP,
                Elements.SERVICE_TASK_SEND_WELCOME_MAIL,
                Elements.END_EVENT_MEMBERSHIP_ACTIVATED,
            )
        verify { claimMembershipUseCase.claim(MembershipId(membershipId)) }
        verify { sendConfirmationMailUseCase.sendConfirmationMail(MembershipId(membershipId)) }
        verify { sendWelcomeMailUseCase.sendWelcomeMail(MembershipId(membershipId)) }
        verify { sendRejectionMailUseCase wasNot Called }
        verify { revokeMembershipRequestUseCase wasNot Called }
        verify { revokeClaimUseCase wasNot Called }
        confirmVerified(
            claimMembershipUseCase,
            sendConfirmationMailUseCase,
            sendWelcomeMailUseCase,
            sendRejectionMailUseCase,
            revokeMembershipRequestUseCase,
            revokeClaimUseCase,
        )
    }

    @Test
    fun `rejection path - no capacity available, gateway routes to rejection mail`() {

        // given - claim returns false, so the gateway takes the no-spots branch
        val membershipId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b87")
        every { claimMembershipUseCase.claim(MembershipId(membershipId)) } returns false

        // when
        processPort.registerMembership(MembershipId(membershipId))
        val instanceKey = awaitProcessInstance(membershipId)

        // then - gateway routed via no-spots, rejection mail sent, process ends rejected
        val instance = ProcessInstanceSelectors.byKey(instanceKey)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        CamundaAssert.assertThatProcessInstance(instance)
            .hasCompletedElements(
                Elements.SERVICE_TASK_CLAIM_MEMBERSHIP,
                Elements.GATEWAY_HAS_EMPTY_SPOTS,
                Elements.SERVICE_TASK_SEND_REJECTION_MAIL,
                Elements.END_EVENT_MEMBERSHIP_REJECTED,
            )
        verify { claimMembershipUseCase.claim(MembershipId(membershipId)) }
        verify { sendRejectionMailUseCase.sendRejectionMail(MembershipId(membershipId)) }
        verify { sendConfirmationMailUseCase wasNot Called }
        verify { sendWelcomeMailUseCase wasNot Called }
        verify { revokeMembershipRequestUseCase wasNot Called }
        verify { revokeClaimUseCase wasNot Called }
        confirmVerified(
            claimMembershipUseCase,
            sendConfirmationMailUseCase,
            sendWelcomeMailUseCase,
            sendRejectionMailUseCase,
            revokeMembershipRequestUseCase,
            revokeClaimUseCase,
        )
    }

    @Test
    fun `user rejects confirmation - request is revoked and claim compensated`() {

        // given
        val membershipId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b88")

        // when - start process, wait for user task, then reject via message
        processPort.registerMembership(MembershipId(membershipId))
        val instanceKey = awaitProcessInstance(membershipId)
        awaitUserTaskCreated(instanceKey)
        processPort.rejectConfirmation(MembershipId(membershipId))

        // then - revoke request + compensation revoke claim run, process ends declined
        val instance = ProcessInstanceSelectors.byKey(instanceKey)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        CamundaAssert.assertThatProcessInstance(instance)
            .hasCompletedElements(
                Elements.SERVICE_TASK_CLAIM_MEMBERSHIP,
                Elements.SERVICE_TASK_SEND_CONFIRMATION_MAIL,
                Elements.EVENT_CONFIRMATION_REJECTED,
                Elements.SERVICE_TASK_REVOKE_MEMBERSHIP_REQUEST,
                Elements.SERVICE_TASK_REVOKE_CLAIM,
                Elements.END_EVENT_MEMBERSHIP_DECLINED,
            )
        verify { claimMembershipUseCase.claim(MembershipId(membershipId)) }
        verify { sendConfirmationMailUseCase.sendConfirmationMail(MembershipId(membershipId)) }
        verify { revokeMembershipRequestUseCase.revokeMembershipRequest(MembershipId(membershipId)) }
        verify { revokeClaimUseCase.revokeClaim(MembershipId(membershipId)) }
        verify { sendWelcomeMailUseCase wasNot Called }
        verify { sendRejectionMailUseCase wasNot Called }
        confirmVerified(
            claimMembershipUseCase,
            sendConfirmationMailUseCase,
            sendWelcomeMailUseCase,
            sendRejectionMailUseCase,
            revokeMembershipRequestUseCase,
            revokeClaimUseCase,
        )
    }

    @Test
    fun `timeout path - after 3 and a half days the request is revoked and claim compensated`() {

        // given
        val membershipId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b89")

        // when - start, wait for user task, then advance time past the 3.5 day deadline
        processPort.registerMembership(MembershipId(membershipId))
        val instanceKey = awaitProcessInstance(membershipId)
        awaitUserTaskCreated(instanceKey)
        processTestContext.increaseTime(Duration.ofDays(3).plusHours(13))

        // then - revoke request + compensation run, process ends declined
        val instance = ProcessInstanceSelectors.byKey(instanceKey)
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        CamundaAssert.assertThatProcessInstance(instance)
            .hasCompletedElements(
                Elements.EVENT_CONFIRMATION_DEADLINE_PASSED,
                Elements.SERVICE_TASK_REVOKE_MEMBERSHIP_REQUEST,
                Elements.SERVICE_TASK_REVOKE_CLAIM,
                Elements.END_EVENT_MEMBERSHIP_DECLINED,
            )
        verify { revokeMembershipRequestUseCase.revokeMembershipRequest(MembershipId(membershipId)) }
        verify { revokeClaimUseCase.revokeClaim(MembershipId(membershipId)) }
        verify { sendWelcomeMailUseCase wasNot Called }
    }

    @Test
    fun `daily reminder - non-interrupting timer triggers re-send without cancelling the user task`() {

        // given
        val membershipId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b90")

        // when - start, wait for user task, then advance one full day so the daily timer fires
        processPort.registerMembership(MembershipId(membershipId))
        val instanceKey = awaitProcessInstance(membershipId)
        awaitUserTaskCreated(instanceKey)
        processTestContext.increaseTime(Duration.ofDays(1).plusHours(1))

        // then - re-send mail worker ran; user task still active (no abort, no compensation)
        val instance = ProcessInstanceSelectors.byKey(instanceKey)
        CamundaAssert.assertThatProcessInstance(instance)
            .hasCompletedElements(
                Elements.SERVICE_TASK_RE_SEND_CONFIRMATION_MAIL,
                Elements.END_EVENT_MAIL_SENT_AGAIN,
            )
        CamundaAssert.assertThatProcessInstance(instance).isActive()
        verify { reSendConfirmationMailUseCase.reSendConfirmationMail(MembershipId(membershipId)) }
        verify { revokeMembershipRequestUseCase wasNot Called }
        verify { revokeClaimUseCase wasNot Called }
    }

    private fun awaitProcessInstance(membershipId: UUID): Long {
        var instanceKey: Long = 0
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofMillis(200))
            .untilAsserted {
                val instances = camundaClient.newProcessInstanceSearchRequest()
                    .filter { filter ->
                        filter.processDefinitionId(MiraveloMembershipProcessApi.PROCESS_ID.value)
                    }
                    .send()
                    .join()
                    .items()
                assert(instances.isNotEmpty()) { "no process instance for membership $membershipId" }
                instanceKey = instances.first().processInstanceKey
            }
        return instanceKey
    }

    private fun awaitUserTaskCreated(processInstanceKey: Long) {
        Awaitility.await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofMillis(200))
            .untilAsserted {
                val tasks = camundaClient.newUserTaskSearchRequest()
                    .filter { filter ->
                        filter.processInstanceKey(processInstanceKey)
                        filter.elementId(Elements.USER_TASK_CONFIRM_MEMBERSHIP.value)
                    }
                    .send()
                    .join()
                    .items()
                assert(tasks.isNotEmpty()) { "user task for $processInstanceKey was not created" }
            }
    }
}
