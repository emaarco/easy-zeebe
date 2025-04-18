package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.SubscriptionId

interface ConfirmSubscriptionUseCase {
    fun confirm(subscriptionId: SubscriptionId)
}