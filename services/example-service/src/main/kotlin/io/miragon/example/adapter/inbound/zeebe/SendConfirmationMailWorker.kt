package io.miragon.example.adapter.inbound.zeebe

import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import io.miragon.example.application.port.inbound.SendConfirmationMailUseCase
import io.miragon.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendConfirmationMailWorker(
    private val useCase: SendConfirmationMailUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.NEWSLETTER_SEND_CONFIRMATION_MAIL)
    fun handle(@Variable subscriptionId: UUID) {
        log.debug { "Received job to send confirmation mail for subscriptionId: $subscriptionId" }
        useCase.sendConfirmationMail(SubscriptionId(subscriptionId))
    }
}
