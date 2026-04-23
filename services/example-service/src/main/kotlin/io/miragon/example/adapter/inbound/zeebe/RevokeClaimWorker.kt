package io.miragon.example.adapter.inbound.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.TaskTypes
import io.miragon.example.application.port.inbound.RevokeClaimUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class RevokeClaimWorker(
    private val useCase: RevokeClaimUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.MIRAVELO_REVOKE_CLAIM)
    fun handle(@Variable membershipId: UUID) {
        log.debug { "Received compensation job to revoke claim for membershipId: $membershipId" }
        useCase.revokeClaim(MembershipId(membershipId))
    }
}
