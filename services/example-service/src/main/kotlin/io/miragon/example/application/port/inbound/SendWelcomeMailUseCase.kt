package io.miragon.example.application.port.inbound

import io.miragon.example.domain.MembershipId

interface SendWelcomeMailUseCase {
    fun sendWelcomeMail(membershipId: MembershipId)
}
