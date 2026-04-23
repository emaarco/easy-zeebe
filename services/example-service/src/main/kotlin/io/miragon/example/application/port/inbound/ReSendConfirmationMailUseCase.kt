package io.miragon.example.application.port.inbound

import io.miragon.example.domain.MembershipId

interface ReSendConfirmationMailUseCase {
    fun reSendConfirmationMail(membershipId: MembershipId)
}
