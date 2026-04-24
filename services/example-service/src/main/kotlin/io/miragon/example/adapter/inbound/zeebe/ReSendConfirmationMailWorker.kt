package io.miragon.example.adapter.inbound.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.ServiceTasks
import io.miragon.example.application.port.inbound.ReSendConfirmationMailUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class ReSendConfirmationMailWorker(
    private val useCase: ReSendConfirmationMailUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MIRAVELO_RE_SEND_CONFIRMATION_MAIL)
    fun handle(@Variable membershipId: UUID) {
        log.debug { "Received job to re-send confirmation mail for membershipId: $membershipId" }
        useCase.reSendConfirmationMail(MembershipId(membershipId))
    }
}
