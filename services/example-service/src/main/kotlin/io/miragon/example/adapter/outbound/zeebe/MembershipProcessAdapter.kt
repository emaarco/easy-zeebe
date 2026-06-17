package io.miragon.example.adapter.outbound.zeebe

import io.camunda.client.CamundaClient
import io.camunda.client.api.search.enums.UserTaskState
import io.miragon.common.zeebe.engine.ProcessEngineApi
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi
import io.miragon.example.application.port.outbound.MembershipProcess
import io.miragon.example.domain.MembershipId
import org.springframework.stereotype.Component

@Component
class MembershipProcessAdapter(
    private val engineApi: ProcessEngineApi,
    private val camundaClient: CamundaClient,
) : MembershipProcess {

    override fun registerMembership(id: MembershipId) {
        engineApi.sendMessage(
            messageName = MiraveloMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_REQUESTED,
            correlationId = id.value.toString(),
            variables = mapOf(MiraveloMembershipProcessApi.Variables.StartEventMembershipRequested.MEMBERSHIP_ID to id.value.toString()),
        )
    }

    override fun confirmMembership(id: MembershipId) {
        val userTaskKey = findActiveConfirmUserTask(id)
        camundaClient.newCompleteUserTaskCommand(userTaskKey).send().join()
    }

    override fun rejectConfirmation(id: MembershipId) {
        engineApi.sendMessage(
            messageName = MiraveloMembershipProcessApi.Messages.MIRAVELO_CONFIRMATION_REJECTED,
            correlationId = id.value.toString(),
        )
    }

    /**
     * Demo: start the membership process directly at the confirmation user task via the
     * process definition (atomic start instruction). Earlier steps (claim, confirmation mail)
     * are skipped. For local experimentation / tests only.
     */
    fun startAtConfirmationViaDefinition(id: MembershipId): Long =
        engineApi.startProcessAtElementViaDefinition(
            processId = MiraveloMembershipProcessApi.PROCESS_ID,
            targetElement = MiraveloMembershipProcessApi.Elements.USER_TASK_CONFIRM_MEMBERSHIP,
            variables = mapOf(
                MiraveloMembershipProcessApi.Variables.StartEventMembershipRequested.MEMBERSHIP_ID to id.value.toString(),
            ),
        )

    /**
     * Demo: start the membership process via its message start event and then move the token
     * to the confirmation user task (process instance modification). For local experimentation
     * / tests only - multi-step and eventually consistent.
     */
    fun startAtConfirmationViaMessage(id: MembershipId): Long =
        engineApi.startProcessAtElementViaMessage(
            messageName = MiraveloMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_REQUESTED,
            correlationId = id.value.toString(),
            targetElement = MiraveloMembershipProcessApi.Elements.USER_TASK_CONFIRM_MEMBERSHIP,
            variables = mapOf(
                MiraveloMembershipProcessApi.Variables.StartEventMembershipRequested.MEMBERSHIP_ID to id.value.toString(),
            ),
        )

    private fun findActiveConfirmUserTask(id: MembershipId): Long {
        return camundaClient.newUserTaskSearchRequest()
            .filter { filter ->
                filter.state(UserTaskState.CREATED)
                filter.elementId(MiraveloMembershipProcessApi.Elements.USER_TASK_CONFIRM_MEMBERSHIP.value)
                filter.processInstanceVariables(
                    mapOf(MiraveloMembershipProcessApi.Variables.StartEventMembershipRequested.MEMBERSHIP_ID.value to id.value.toString())
                )
            }
            .send()
            .join()
            .items()
            .single()
            .userTaskKey
    }
}
