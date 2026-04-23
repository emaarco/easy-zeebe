package io.miragon.example.application.service

import io.miragon.example.application.port.inbound.RevokeClaimUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class RevokeClaimService : RevokeClaimUseCase {

    private val log = KotlinLogging.logger {}

    override fun revokeClaim(membershipId: MembershipId) {
        log.info { "Revoking capacity claim for $membershipId" }
    }
}
