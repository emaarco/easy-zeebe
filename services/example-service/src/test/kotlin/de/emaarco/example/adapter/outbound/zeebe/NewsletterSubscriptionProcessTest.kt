package de.emaarco.example.adapter.outbound.zeebe

import de.emaarco.common.test.zeebe.ZeebeProcessTest
import de.emaarco.common.zeebe.engine.ProcessEngineApi
import de.emaarco.common.zeebe.worker.DefaultJobWorker
import de.emaarco.example.adapter.inbound.zeebe.AbortRegistrationWorker
import de.emaarco.example.adapter.inbound.zeebe.SendConfirmationMailWorker
import de.emaarco.example.adapter.inbound.zeebe.SendWelcomeMailWorker
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements.Activity_AbortRegistration
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements.Activity_ConfirmRegistration
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements.Activity_SendConfirmationMail
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements.Activity_SendWelcomeMail
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements.EndEvent_RegistrationAborted
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements.EndEvent_RegistrationCompleted
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements.StartEvent_RequestReceived
import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.application.port.inbound.SendWelcomeMailUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.zeebe.client.ZeebeClient
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*

/**
 * An example test for the NewsletterSubscriptionProcess.
 * It uses the common-zeebe-test module to test the process
 */
class NewsletterSubscriptionProcessTest : ZeebeProcessTest(
    processes = listOf("bpmn/newsletter.bpmn"),
) {

    private lateinit var processPort: NewsletterSubscriptionProcessAdapter
    private val sendConfirmationMailUseCase = mockk<SendConfirmationMailUseCase>()
    private val sendWelcomeMailUseCase = mockk<SendWelcomeMailUseCase>()
    private val abortSubscriptionUseCase = mockk<AbortSubscriptionUseCase>()

    override val jobWorkers: List<DefaultJobWorker> = listOf(
        SendConfirmationMailWorker(sendConfirmationMailUseCase),
        SendWelcomeMailWorker(sendWelcomeMailUseCase),
        AbortRegistrationWorker(abortSubscriptionUseCase)
    )

    override fun initProcessPort(client: ZeebeClient) {
        val processEngineApi = ProcessEngineApi(client)
        this.processPort = NewsletterSubscriptionProcessAdapter(processEngineApi)
    }

    override fun beforeEach() {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } just Runs
        every { sendWelcomeMailUseCase.sendWelcomeMail(any()) } just Runs
        every { abortSubscriptionUseCase.abort(any()) } just Runs
    }

    @Test
    fun `happy path - user subscribes to newsletter`() {

        val subscriptionId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b86")
        processPort.submitForm(SubscriptionId(subscriptionId))

        waitForProcessInstanceHasReachedElement(Activity_ConfirmRegistration)
        processPort.confirmSubscription(SubscriptionId(subscriptionId))

        waitForProcessInstanceHasPassedElement(EndEvent_RegistrationCompleted)
        assertThatProcess().isCompleted()
        assertThatProcess().hasPassedElementsInOrder(
            StartEvent_RequestReceived,
            Activity_SendConfirmationMail,
            Activity_ConfirmRegistration,
            Activity_SendWelcomeMail,
            EndEvent_RegistrationCompleted
        )

        verify { sendConfirmationMailUseCase.sendConfirmationMail(SubscriptionId(subscriptionId)) }
        verify { sendWelcomeMailUseCase.sendWelcomeMail(SubscriptionId(subscriptionId)) }
        verify { abortSubscriptionUseCase wasNot Called }
        confirmVerified(sendConfirmationMailUseCase, sendWelcomeMailUseCase, abortSubscriptionUseCase)
    }

    @Test
    fun `abort registration if user has not confirmed after 3 minutes`() {

        val subscriptionId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b87")
        processPort.submitForm(SubscriptionId(subscriptionId))
        waitForProcessInstanceHasReachedElement(Activity_ConfirmRegistration)

        // We resend the confirmation mail after 1 minute
        // If the user does not confirm after 2:30 minutes, we abort the registration
        increaseTime(Duration.ofMinutes(1))
        waitForProcessInstanceHasPassedElement(Activity_SendConfirmationMail, times = 2)
        increaseTime(Duration.ofMinutes(1))
        waitForProcessInstanceHasPassedElement(Activity_SendConfirmationMail, times = 3)
        increaseTime(Duration.ofSeconds(30))

        waitForProcessInstanceHasPassedElement(EndEvent_RegistrationAborted)
        assertThatProcess().isCompleted()
        assertThatProcess().hasPassedElementsInOrder(
            StartEvent_RequestReceived,
            Activity_SendConfirmationMail,
            Activity_SendConfirmationMail,
            Activity_SendConfirmationMail,
            Activity_AbortRegistration,
            EndEvent_RegistrationAborted
        )

        verify { sendConfirmationMailUseCase.sendConfirmationMail(SubscriptionId(subscriptionId)) }
        verify { abortSubscriptionUseCase.abort(SubscriptionId(subscriptionId)) }
        verify { sendWelcomeMailUseCase wasNot Called }
        confirmVerified(sendConfirmationMailUseCase, sendWelcomeMailUseCase, abortSubscriptionUseCase)
    }
}