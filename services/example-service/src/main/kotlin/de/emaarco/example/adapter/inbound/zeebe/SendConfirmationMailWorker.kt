package de.emaarco.example.adapter.inbound.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.domain.SubscriptionId
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

    @JobWorker(type = TaskTypes.ACTIVITY_SEND_CONFIRMATION_MAIL)
    fun handle(@Variable subscriptionId: UUID) {
        log.debug { "Received job to send confirmation mail for subscriptionId: $subscriptionId" }
        useCase.sendConfirmationMail(SubscriptionId(subscriptionId))
    }
}
