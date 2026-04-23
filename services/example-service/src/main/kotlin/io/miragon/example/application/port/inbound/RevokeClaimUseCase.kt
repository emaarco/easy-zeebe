package io.miragon.example.application.port.inbound

import io.miragon.example.domain.MembershipId

interface RevokeClaimUseCase {
    fun revokeClaim(membershipId: MembershipId)
}
