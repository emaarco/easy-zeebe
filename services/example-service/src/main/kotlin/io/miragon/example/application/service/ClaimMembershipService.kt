package io.miragon.example.application.service

import io.miragon.example.application.port.inbound.ClaimMembershipUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

@Service
class ClaimMembershipService : ClaimMembershipUseCase {

    private val log = KotlinLogging.logger {}
    private val claimedSpots = AtomicInteger(0)
    private val maxSpots = 1000

    override fun claim(membershipId: MembershipId): Boolean {
        val claimed = claimedSpots.incrementAndGet()
        val hasSpot = claimed <= maxSpots
        log.info { "Claiming membership for $membershipId – spot $claimed/$maxSpots, hasSpot=$hasSpot" }
        return hasSpot
    }
}
