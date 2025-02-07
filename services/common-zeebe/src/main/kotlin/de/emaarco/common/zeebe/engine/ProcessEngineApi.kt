package de.emaarco.common.zeebe.engine

import io.camunda.zeebe.client.ZeebeClient
import java.time.Duration
import java.time.temporal.ChronoUnit

class ProcessEngineApi(
    private val zeebeClient: ZeebeClient,
) {

    /**
     * Use this method to start a process instance via a message start-event.
     * @param messageName the id of the message that should start the process
     * @param variables the variables that should be passed to the process
     */
    fun startProcessViaMessage(
        messageName: String,
        variables: Map<String, Any> = emptyMap(),
    ) {
        zeebeClient.newPublishMessageCommand()
            .messageName(messageName)
            .withoutCorrelationKey()
            .variables(variables)
            .send()
            .join()
    }

    /**
     * Use this method to send a message to a running process instance.
     * @param messageName the id of the message that should be sent
     * @param correlationId an id that is used to identify the process instance
     * @param variables the variables that should be passed to the process
     */
    fun sendMessage(
        messageName: String,
        correlationId: String,
        variables: Map<String, Any> = emptyMap(),
    ) {
        zeebeClient.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(correlationId)
            .variables(variables)
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }
}