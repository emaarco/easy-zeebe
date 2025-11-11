package de.emaarco.example.adapter.inbound.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class AbortRegistrationWorker(
    private val useCase: AbortSubscriptionUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.Activity_AbortRegistration)
    fun handle(@Variable subscriptionId: UUID) {
        log.debug { "Received job to abort registration for subscriptionId: $subscriptionId" }
        useCase.abort(SubscriptionId(subscriptionId))
    }
}
