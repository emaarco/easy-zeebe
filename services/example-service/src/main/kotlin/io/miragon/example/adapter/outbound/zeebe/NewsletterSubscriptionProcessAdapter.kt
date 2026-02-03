package io.miragon.example.adapter.outbound.zeebe

import io.miragon.common.zeebe.engine.ProcessEngineApi
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi
import io.miragon.example.application.port.outbound.NewsletterSubscriptionProcess
import io.miragon.example.domain.SubscriptionId
import org.springframework.stereotype.Component

@Component
class NewsletterSubscriptionProcessAdapter(
    private val engineApi: ProcessEngineApi
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId): Long {
        val variables = mapOf("subscriptionId" to id.value.toString())
        return engineApi.startProcess(
            processId = NewsletterSubscriptionProcessApi.PROCESS_ID,
            variables = variables
        )
    }

    override fun confirmSubscription(id: SubscriptionId) {
        engineApi.sendMessage(
            messageName = NewsletterSubscriptionProcessApi.Messages.MESSAGE_SUBSCRIPTION_CONFIRMED,
            correlationId = id.value.toString(),
        )
    }
}