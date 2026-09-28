package io.miragon.example.adapter.outbound.zeebe

import io.camunda.client.CamundaClient
import io.camunda.client.api.search.enums.UserTaskState
import io.miragon.common.zeebe.engine.ProcessEngineApi
import io.miragon.example.adapter.process.Messages
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
            messageName = Messages.MIRAVELO_MEMBERSHIP_REQUESTED,
            correlationId = id.value.toString(),
            variables = mapOf(MiraveloMembershipProcessApi.Flow.StartEventMembershipRequested.Variables.MEMBERSHIP_ID to id.value.toString()),
        )
    }

    override fun confirmMembership(id: MembershipId) {
        val userTaskKey = findActiveConfirmUserTask(id)
        camundaClient.newCompleteUserTaskCommand(userTaskKey).send().join()
    }

    override fun rejectConfirmation(id: MembershipId) {
        engineApi.sendMessage(
            messageName = Messages.MIRAVELO_CONFIRMATION_REJECTED,
            correlationId = id.value.toString(),
        )
    }

    private fun findActiveConfirmUserTask(id: MembershipId): Long {
        return camundaClient.newUserTaskSearchRequest()
            .filter { filter ->
                filter.state(UserTaskState.CREATED)
                filter.elementId(MiraveloMembershipProcessApi.Flow.UserTaskConfirmMembership.id.value)
                filter.processInstanceVariables(
                    mapOf(MiraveloMembershipProcessApi.Flow.StartEventMembershipRequested.Variables.MEMBERSHIP_ID.value to "\"${id.value}\"")
                )
            }
            .send()
            .join()
            .items()
            .single()
            .userTaskKey
    }
}
