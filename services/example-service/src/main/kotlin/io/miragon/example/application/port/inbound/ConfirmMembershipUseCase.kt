package io.miragon.example.application.port.inbound

import io.miragon.example.domain.MembershipId

interface ConfirmMembershipUseCase {
    fun confirm(membershipId: MembershipId)
}
