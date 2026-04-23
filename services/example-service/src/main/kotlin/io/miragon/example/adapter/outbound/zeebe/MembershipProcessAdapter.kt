package io.miragon.example.adapter.outbound.zeebe

import io.miragon.common.zeebe.engine.ProcessEngineApi
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi
import io.miragon.example.application.port.outbound.MembershipProcess
import io.miragon.example.domain.MembershipId
import org.springframework.stereotype.Component

@Component
class MembershipProcessAdapter(
    private val engineApi: ProcessEngineApi
) : MembershipProcess {

    override fun registerMembership(id: MembershipId): Long {
        val variables = mapOf(MiraveloMembershipProcessApi.Variables.MEMBERSHIP_ID to id.value.toString())
        return engineApi.startProcess(
            processId = MiraveloMembershipProcessApi.PROCESS_ID,
            variables = variables
        )
    }

    override fun confirmMembership(id: MembershipId) {
        engineApi.sendMessage(
            messageName = MiraveloMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED,
            correlationId = id.value.toString(),
        )
    }
}
