package io.miragon.example.application.port.inbound

import io.miragon.example.domain.MembershipId

interface ClaimMembershipUseCase {
    fun claim(membershipId: MembershipId): Boolean
}
