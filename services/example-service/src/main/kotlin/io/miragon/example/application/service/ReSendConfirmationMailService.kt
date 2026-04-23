package io.miragon.example.application.service

import io.miragon.example.application.port.inbound.ReSendConfirmationMailUseCase
import io.miragon.example.application.port.outbound.MembershipRepository
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ReSendConfirmationMailService(
    private val repository: MembershipRepository,
) : ReSendConfirmationMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun reSendConfirmationMail(membershipId: MembershipId) {
        val membership = repository.find(membershipId)
        log.info { "Re-sending confirmation mail to ${membership.email}" }
    }
}
