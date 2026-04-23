package io.miragon.example.application.service

import io.miragon.example.application.port.inbound.RejectConfirmationUseCase
import io.miragon.example.application.port.outbound.MembershipProcess
import io.miragon.example.application.port.outbound.MembershipRepository
import io.miragon.example.domain.MembershipId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class RejectConfirmationService(
    private val repository: MembershipRepository,
    private val processPort: MembershipProcess,
) : RejectConfirmationUseCase {

    private val log = KotlinLogging.logger {}

    override fun rejectConfirmation(membershipId: MembershipId) {
        val membership = repository.find(membershipId).rejectMembership()
        repository.save(membership)
        processPort.rejectConfirmation(membership.id)
        log.info { "Rejected confirmation for membership ${membership.id}" }
    }
}
