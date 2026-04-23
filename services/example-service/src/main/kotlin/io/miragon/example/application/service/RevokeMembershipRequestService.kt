package io.miragon.example.application.service

import io.miragon.example.application.port.inbound.RevokeMembershipRequestUseCase
import io.miragon.example.application.port.outbound.MembershipRepository
import io.miragon.example.domain.MembershipId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class RevokeMembershipRequestService(
    private val repository: MembershipRepository,
) : RevokeMembershipRequestUseCase {

    private val log = KotlinLogging.logger {}

    override fun revokeMembershipRequest(membershipId: MembershipId) {
        val membership = repository.find(membershipId).declineMembership()
        repository.save(membership)
        log.info { "Revoked membership request for ${membership.id}" }
    }
}
