package de.emaarco.example.adapter.inbound.zeebe

import de.emaarco.common.zeebe.worker.DefaultJobWorker
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes.Activity_SendConfirmationMail
import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.JobClient
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendConfirmationMailWorker(
    private val useCase: SendConfirmationMailUseCase
) : DefaultJobWorker(
    type = Activity_SendConfirmationMail
) {

    private val log = KotlinLogging.logger {}

    override fun executeTask(client: JobClient, job: ActivatedJob): Map<String, Any?> {
        val input = job.getVariablesAsType(Input::class.java)
        log.debug { "Received job to send confirmation mail: $input" }
        useCase.sendConfirmationMail(SubscriptionId(input.subscriptionId))
        return emptyMap()
    }

    data class Input(
        val subscriptionId: UUID
    )
}