package io.miragon.example.application.port.inbound

import io.miragon.example.domain.SubscriptionId

interface SendWelcomeMailUseCase {
    fun sendWelcomeMail(subscriptionId: SubscriptionId)
}