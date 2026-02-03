package io.miragon.example.application.port.inbound

import io.miragon.example.domain.SubscriptionId

interface AbortSubscriptionUseCase {
    fun abort(subscriptionId: SubscriptionId)
}