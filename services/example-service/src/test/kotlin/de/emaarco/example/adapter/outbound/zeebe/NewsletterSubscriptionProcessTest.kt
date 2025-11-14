package de.emaarco.example.adapter.outbound.zeebe

import com.ninjasquad.springmockk.MockkBean
import de.emaarco.common.test.config.TestProcessEngineConfiguration
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements.ACTIVITY_ABORT_REGISTRATION
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Elements.ACTIVITY_SEND_CONFIRMATION_MAIL
import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.application.port.inbound.SendWelcomeMailUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.process.test.api.CamundaAssert
import io.camunda.process.test.api.CamundaProcessTestContext
import io.camunda.process.test.api.CamundaSpringProcessTest
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors.byKey
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
import java.util.*

/**
 * Process test for the Newsletter Subscription Process.
 * Uses native Camunda 8.8 test API with @CamundaSpringProcessTest and Spring Boot for component injection.
 * Workers are automatically registered via @JobWorker annotation.
 * Uses H2 an in-memory database for testing (configured in test/resources/application.yaml).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@CamundaSpringProcessTest
@Import(TestProcessEngineConfiguration::class)
class NewsletterSubscriptionProcessTest {

    @Autowired
    private lateinit var processTestContext: CamundaProcessTestContext

    @Autowired
    private lateinit var processPort: NewsletterSubscriptionProcessAdapter

    @MockkBean
    private lateinit var sendConfirmationMailUseCase: SendConfirmationMailUseCase

    @MockkBean
    private lateinit var sendWelcomeMailUseCase: SendWelcomeMailUseCase

    @MockkBean
    private lateinit var abortSubscriptionUseCase: AbortSubscriptionUseCase

    @BeforeEach
    fun setup() {
        every { sendConfirmationMailUseCase.sendConfirmationMail(any()) } just Runs
        every { sendWelcomeMailUseCase.sendWelcomeMail(any()) } just Runs
        every { abortSubscriptionUseCase.abort(any()) } just Runs
    }

    @Test
    fun `happy path - user subscribes to newsletter`() {

        // given
        val subscriptionId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b86")

        // when - start a process via undefined start event
        val instanceKey = processPort.submitForm(SubscriptionId(subscriptionId))

        // then - process should be active
        val instance = byKey(instanceKey)
        CamundaAssert.assertThatProcessInstance(instance).isActive()

        // when - confirm subscription
        processPort.confirmSubscription(SubscriptionId(subscriptionId))

        // Verify use cases were called
        CamundaAssert.assertThatProcessInstance(instance).isCompleted()
        verify { sendConfirmationMailUseCase.sendConfirmationMail(SubscriptionId(subscriptionId)) }
        verify { sendWelcomeMailUseCase.sendWelcomeMail(SubscriptionId(subscriptionId)) }
        verify { abortSubscriptionUseCase wasNot Called }
        confirmVerified(sendConfirmationMailUseCase, sendWelcomeMailUseCase, abortSubscriptionUseCase)
    }

    @Test
    fun `abort registration if user has not confirmed after 3 minutes`() {

        // given
        val subscriptionId = UUID.fromString("4a607799-804b-43d1-8aa2-bdcc4dfd9b87")

        // when - start process via message
        val instanceKey = processPort.submitForm(SubscriptionId(subscriptionId))

        // then - let 3 minutes pass to send mails
        processTestContext.increaseTime(Duration.ofSeconds(60))
        CamundaAssert
            .assertThatProcessInstance(byKey(instanceKey))
            .hasCompletedElement(ACTIVITY_SEND_CONFIRMATION_MAIL, 1)

        processTestContext.increaseTime(Duration.ofSeconds(60))
        CamundaAssert.assertThatProcessInstance(byKey(instanceKey))
            .hasCompletedElement(ACTIVITY_SEND_CONFIRMATION_MAIL, 2)

        processTestContext.increaseTime(Duration.ofSeconds(30))
        CamundaAssert.assertThatProcessInstance(byKey(instanceKey))
            .hasCompletedElement(ACTIVITY_ABORT_REGISTRATION, 1)

        // then - process should abort
        CamundaAssert.assertThatProcessInstance(byKey(instanceKey)).isCompleted
        verify(exactly = 2) { sendConfirmationMailUseCase.sendConfirmationMail(SubscriptionId(subscriptionId)) }
        verify { abortSubscriptionUseCase.abort(SubscriptionId(subscriptionId)) }
        verify { sendWelcomeMailUseCase wasNot Called }
        confirmVerified(sendConfirmationMailUseCase, sendWelcomeMailUseCase, abortSubscriptionUseCase)
    }
}
