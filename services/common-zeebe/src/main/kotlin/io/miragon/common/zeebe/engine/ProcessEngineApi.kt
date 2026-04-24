package io.miragon.common.zeebe.engine

import io.github.emaarco.bpmn.runtime.MessageName
import io.github.emaarco.bpmn.runtime.ProcessId
import io.github.emaarco.bpmn.runtime.VariableName
import io.miragon.common.zeebe.context.EventualConsistent
import io.miragon.common.zeebe.context.StronglyConsistent
import io.camunda.client.CamundaClient
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
    @StronglyConsistent
    open fun startProcess(
        processId: ProcessId,
        variables: Map<VariableName, Any> = emptyMap(),
    ): Long {
        return camundaClient.newCreateInstanceCommand()
            .bpmnProcessId(processId.value)
            .latestVersion()
            .variables(variables.mapKeys { it.key.value })
            .send()
            .join()
            .processInstanceKey
    }

    /**
     * Use this method to send a message to a running process instance.
     * @param messageName the name of the message that should be sent
     * @param correlationId an id that is used to identify the process instance
     * @param variables the variables that should be passed to the process
     */
    @StronglyConsistent
    open fun sendMessage(
        messageName: MessageName,
        correlationId: String,
        variables: Map<VariableName, Any> = emptyMap(),
    ) {
        camundaClient.newPublishMessageCommand()
            .messageName(messageName.value)
            .correlationKey(correlationId)
            .variables(variables.mapKeys { it.key.value })
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }

    /**
     * Use this method to search for process instances.
     * For usage in production I would recommend using filtering.
     * @return a list of all process instances
     */
    @EventualConsistent
    open fun searchForProcessInstances(): List<ProcessInstance> {
        val instances = camundaClient.newProcessInstanceSearchRequest().send().join()
        return instances.items()
    }

}