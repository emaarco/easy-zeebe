package io.miragon.example.adapter.inbound.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Variables
import io.miragon.example.application.port.inbound.SendWelcomeMailUseCase
import io.miragon.example.domain.SubscriptionId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendWelcomeMailWorker(
    private val useCase: SendWelcomeMailUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.NEWSLETTER_SEND_WELCOME_MAIL)
    fun handle(@Variable subscriptionId: UUID): Map<String, Any> {
        log.debug { "Received job to send welcome mail for subscriptionId: $subscriptionId" }
        useCase.sendWelcomeMail(SubscriptionId(subscriptionId))
        return mapOf(Variables.WELCOME_MAIL_SENT to true)
    }
}
