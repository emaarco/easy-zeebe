package io.miragon.example.application.port.outbound

import io.miragon.example.domain.SubscriptionId

interface NewsletterSubscriptionProcess {
    fun submitForm(id: SubscriptionId): Long
    fun confirmSubscription(id: SubscriptionId)
}