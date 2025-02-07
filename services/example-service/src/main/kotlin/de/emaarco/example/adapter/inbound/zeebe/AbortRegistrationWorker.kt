package de.emaarco.example.adapter.inbound.zeebe

import de.emaarco.common.zeebe.worker.DefaultJobWorker
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi
import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.JobClient
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class AbortRegistrationWorker(
    private val useCase: AbortSubscriptionUseCase
) : DefaultJobWorker(
    type = NewsletterSubscriptionProcessApi.TaskTypes.Activity_AbortRegistration,
) {

    private val log = KotlinLogging.logger {}

    override fun executeTask(client: JobClient, job: ActivatedJob): Map<String, Any?> {
        val input = job.getVariablesAsType(Input::class.java)
        log.debug { "Received job to abort registration: $input" }
        useCase.abort(SubscriptionId(input.subscriptionId))
        return emptyMap()
    }

    data class Input(
        val subscriptionId: UUID
    )
}