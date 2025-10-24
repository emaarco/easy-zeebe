package de.emaarco.common.zeebe.engine

import io.camunda.client.CamundaClient
import io.camunda.client.api.ExperimentalApi
import io.camunda.client.api.search.response.ProcessInstance
import java.time.Duration
import java.time.temporal.ChronoUnit

open class ProcessEngineApi(
    private val camundaClient: CamundaClient,
) {

    /**
     * Use this method to start a process instance via an undefined start event
     * @param processId the id of the process to start
     * @param variables the variables that should be passed to the process
     * @return the key of the process instance
     */
    open fun startProcess(
        processId: String,
        variables: Map<String, Any> = emptyMap(),
    ): Long {
        return camundaClient.newCreateInstanceCommand()
            .bpmnProcessId(processId)
            .latestVersion()
            .variables(variables)
            .send()
            .join()
            .processInstanceKey
    }

    /**
     * Use this method to send a message to a running process instance.
     * @param messageName the id of the message that should be sent
     * @param correlationId an id that is used to identify the process instance
     * @param variables the variables that should be passed to the process
     */
    open fun sendMessage(
        messageName: String,
        correlationId: String,
        variables: Map<String, Any> = emptyMap(),
    ) {
        camundaClient.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(correlationId)
            .variables(variables)
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }

    /**
     * Use this method to search for process instances
     * @return a list of process instances
     */
    @ExperimentalApi("Stil in alpha in zeebe 8.8")
    open fun searchProcessInstances(): List<ProcessInstance> {
        TODO("Not yet implemented")
    }
}