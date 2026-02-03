package io.miragon.example.application.port.inbound

import io.miragon.example.domain.SubscriptionId

interface SendConfirmationMailUseCase {
    fun sendConfirmationMail(subscriptionId: SubscriptionId)
}