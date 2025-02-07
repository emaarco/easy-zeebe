package de.emaarco.example.application.port.inbound

import de.emaarco.example.domain.SubscriptionId

interface SendConfirmationMailUseCase {
    fun sendConfirmationMail(subscriptionId: SubscriptionId)
}