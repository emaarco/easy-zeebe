package io.miragon.example.adapter.inbound.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.ServiceTasks
import io.miragon.example.application.port.inbound.RevokeMembershipRequestUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class RevokeMembershipRequestWorker(
    private val useCase: RevokeMembershipRequestUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MIRAVELO_REVOKE_MEMBERSHIP_REQUEST)
    fun handle(@Variable membershipId: UUID) {
        log.debug { "Received job to revoke membership request for membershipId: $membershipId" }
        useCase.revokeMembershipRequest(MembershipId(membershipId))
    }
}
