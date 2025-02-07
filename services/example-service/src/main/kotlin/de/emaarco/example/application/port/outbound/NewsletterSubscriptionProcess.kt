package de.emaarco.example.application.port.outbound

import de.emaarco.example.domain.SubscriptionId

interface NewsletterSubscriptionProcess {
    fun submitForm(id: SubscriptionId)
    fun confirmSubscription(id: SubscriptionId)
}