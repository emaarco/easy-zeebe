package io.miragon.example.adapter.inbound.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.ServiceTasks
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.Variables
import io.miragon.example.application.port.inbound.ClaimMembershipUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class ClaimMembershipWorker(
    private val useCase: ClaimMembershipUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MIRAVELO_CLAIM_MEMBERSHIP)
    fun handle(@Variable membershipId: UUID): Map<String, Any> {
        log.debug { "Received job to claim membership for membershipId: $membershipId" }
        val hasEmptySpots = useCase.claim(MembershipId(membershipId))
        return mapOf(Variables.ServiceTaskClaimMembership.HAS_EMPTY_SPOTS.value to hasEmptySpots)
    }
}
