package io.miragon.example.application.port.inbound

import io.miragon.example.domain.MembershipId

interface SendRejectionMailUseCase {
    fun sendRejectionMail(membershipId: MembershipId)
}
