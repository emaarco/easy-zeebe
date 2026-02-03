package io.miragon.example.application.port.outbound

import io.miragon.example.domain.NewsletterSubscription
import io.miragon.example.domain.SubscriptionId

interface NewsletterSubscriptionRepository {
    fun find(subscriptionId: SubscriptionId): NewsletterSubscription
    fun search(subscriptionId: SubscriptionId): NewsletterSubscription?
    fun save(subscription: NewsletterSubscription)
    fun delete(subscriptionId: SubscriptionId)
}