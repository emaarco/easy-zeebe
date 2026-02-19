package io.miragon.example.adapter.outbound.zeebe

import io.miragon.common.zeebe.engine.ProcessEngineApi
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi
import io.miragon.example.application.port.outbound.NewsletterSubscriptionProcess
import io.miragon.example.domain.SubscriptionId
import org.springframework.stereotype.Component

// --- startProcess variant ---
// Inject ProcessEngineApi; implement the outbound port interface.
// Method accepts domain types; extract .value.toString() for the variables map.
@Component
class NewsletterSubscriptionProcessAdapter(
    private val engineApi: ProcessEngineApi
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId): Long {
        val variables = mapOf(NewsletterSubscriptionProcessApi.Variables.SUBSCRIPTION_ID to id.value.toString())
        return engineApi.startProcess(
            processId = NewsletterSubscriptionProcessApi.PROCESS_ID,
            variables = variables
        )
    }

    // --- sendMessage variant ---
    // Always pass correlationId to correlate with the right process instance.
    // messageName and variable keys must come from ProcessApi constants — never raw strings.
    override fun confirmSubscription(id: SubscriptionId) {
        engineApi.sendMessage(
            messageName = NewsletterSubscriptionProcessApi.Messages.NEWSLETTER_SUBSCRIPTION_CONFIRMED,
            correlationId = id.value.toString(),
        )
    }

}
