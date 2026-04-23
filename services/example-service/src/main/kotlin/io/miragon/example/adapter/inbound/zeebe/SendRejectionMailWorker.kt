package io.miragon.example.adapter.inbound.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.TaskTypes
import io.miragon.example.application.port.inbound.SendRejectionMailUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendRejectionMailWorker(
    private val useCase: SendRejectionMailUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.MIRAVELO_SEND_REJECTION_MAIL)
    fun handle(@Variable membershipId: UUID) {
        log.debug { "Received job to send rejection mail for membershipId: $membershipId" }
        useCase.sendRejectionMail(MembershipId(membershipId))
    }
}
