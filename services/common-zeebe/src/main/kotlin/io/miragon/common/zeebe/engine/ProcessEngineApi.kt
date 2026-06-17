package io.miragon.common.zeebe.engine

import io.github.emaarco.bpmn.runtime.ElementId
import io.github.emaarco.bpmn.runtime.MessageName
import io.github.emaarco.bpmn.runtime.ProcessId
import io.github.emaarco.bpmn.runtime.VariableName
import io.miragon.common.zeebe.context.EventualConsistent
import io.miragon.common.zeebe.context.StronglyConsistent
import io.camunda.client.CamundaClient
import io.camunda.client.api.search.enums.ElementInstanceState
import io.camunda.client.api.search.enums.ElementInstanceType
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
     * Starts a process instance via its definition directly BEFORE the given element
     * (Camunda start instruction). Elements before the target are skipped.
     *
     * This happens in a single, atomic command. It is intended for local experimentation
     * and tests - NOT for regular production use (see Camunda's "run a process segment").
     *
     * Note: variables that earlier (skipped) tasks would normally set must be provided
     * here, otherwise gateways / IO-mappings after the target may raise incidents.
     *
     * @param processId the id of the process to start
     * @param targetElement the element the instance should be started at (started "before" it)
     * @param variables the variables that should be passed to the process
     * @return the key of the process instance
     */
    @StronglyConsistent
    open fun startProcessAtElementViaDefinition(
        processId: ProcessId,
        targetElement: ElementId,
        variables: Map<VariableName, Any> = emptyMap(),
    ): Long {
        return camundaClient.newCreateInstanceCommand()
            .bpmnProcessId(processId.value)
            .latestVersion()
            .startBeforeElement(targetElement.value)
            .variables(variables.mapKeys { it.key.value })
            .send()
            .join()
            .processInstanceKey
    }

    /**
     * Starts a process instance via a message (at its message start event) and then moves
     * the token to the given target element using process instance modification.
     *
     * ATTENTION: this is multi-step and NOT atomic. It is a pure test / intervention tool.
     * After the message-start the instance keeps running, so it is moved from whatever leaf
     * element it currently waits at - this is only deterministic if the process settles at a
     * single stable wait state (the common case; we wait for that state before moving). The
     * token is moved by element id (the engine resolves the live element instances), with the
     * target's ancestor scope inferred - so we never reference stale element instance keys.
     *
     * @param messageName the message that triggers the message start event
     * @param correlationId the correlation key for the message
     * @param targetElement the element the token should be moved to
     * @param variables the variables that should be passed to the process
     * @return the key of the process instance
     */
    @StronglyConsistent
    open fun startProcessAtElementViaMessage(
        messageName: MessageName,
        correlationId: String,
        targetElement: ElementId,
        variables: Map<VariableName, Any> = emptyMap(),
    ): Long {
        // 1) start the instance at the message start event; correlateMessage returns the instance key
        val processInstanceKey = camundaClient.newCorrelateMessageCommand()
            .messageName(messageName.value)
            .correlationKey(correlationId)
            .variables(variables.mapKeys { it.key.value })
            .send()
            .join()
            .processInstanceKey
        // 2) once the instance has settled at its wait state, move the token to the target
        moveTokenToTarget(processInstanceKey, targetElement)
        return processInstanceKey
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

    /**
     * Waits until the instance sits at a single active leaf element, then - unless it is already
     * the target - moves the token there. The move is issued by element id (not instance key), so
     * the engine resolves the live element instances itself; the call is retried because the
     * element-instance search we read the current position from is eventually consistent.
     */
    private fun moveTokenToTarget(processInstanceKey: Long, targetElement: ElementId) {
        var previous: String? = null
        await(SEARCH_TIMEOUT) {
            val source = singleActiveLeafElementId(processInstanceKey)
            val stable = source != null && source == previous // only act on a settled wait state
            previous = source
            when {
                source == null || !stable -> null // not settled yet (or a transient element) -> keep polling
                source == targetElement.value -> DONE // already settled at the target
                else -> runCatching {
                    camundaClient.newModifyProcessInstanceCommand(processInstanceKey)
                        .moveElementsWithInferredAncestor(source, targetElement.value)
                        .send()
                        .join()
                    DONE
                }.getOrNull() // null on rejection (token already moved on) -> keep retrying
            }
        }
    }

    /** The element id of the single active leaf (non-container) element, or null if not exactly one. */
    private fun singleActiveLeafElementId(processInstanceKey: Long): String? =
        camundaClient.newElementInstanceSearchRequest()
            .filter { filter ->
                filter.processInstanceKey(processInstanceKey)
                filter.state(ElementInstanceState.ACTIVE)
            }
            .send()
            .join()
            .items()
            .filter { it.type !in CONTAINER_ELEMENT_TYPES }
            .singleOrNull()
            ?.elementId

    private fun <T : Any> await(timeout: Duration, action: () -> T?): T {
        val deadline = System.currentTimeMillis() + timeout.toMillis()
        while (System.currentTimeMillis() < deadline) {
            action()?.let { return it }
            Thread.sleep(POLL_INTERVAL_MILLIS)
        }
        throw IllegalStateException("Timed out after $timeout while waiting for the process engine state")
    }

    private companion object {
        private val SEARCH_TIMEOUT: Duration = Duration.ofSeconds(30)
        private const val POLL_INTERVAL_MILLIS: Long = 200

        /** Non-null sentinel so [await] can signal "done" from an action that has no result. */
        private val DONE = Any()

        /** Scope/container element types that hold no token themselves. */
        private val CONTAINER_ELEMENT_TYPES = setOf(
            ElementInstanceType.PROCESS,
            ElementInstanceType.SUB_PROCESS,
            ElementInstanceType.EVENT_SUB_PROCESS,
            ElementInstanceType.AD_HOC_SUB_PROCESS,
            ElementInstanceType.MULTI_INSTANCE_BODY,
        )
    }

}