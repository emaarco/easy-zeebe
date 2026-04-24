package io.miragon.example.adapter.inbound.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.ServiceTasks
import io.miragon.example.application.port.inbound.SendWelcomeMailUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendWelcomeMailWorker(
    private val useCase: SendWelcomeMailUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MIRAVELO_SEND_WELCOME_MAIL)
    fun handle(@Variable membershipId: UUID) {
        log.debug { "Received job to send welcome mail for membershipId: $membershipId" }
        useCase.sendWelcomeMail(MembershipId(membershipId))
    }
}
