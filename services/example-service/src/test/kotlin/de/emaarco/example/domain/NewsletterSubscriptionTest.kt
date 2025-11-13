package de.emaarco.example.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NewsletterSubscriptionTest {

    @Test
    fun `confirm registration changes status to CONFIRMED`() {
        val subscription = testNewsletterSubscription(status = SubscriptionStatus.PENDING)
        val confirmed = subscription.confirmRegistration()
        assertThat(confirmed).isEqualTo(subscription.copy(status = SubscriptionStatus.CONFIRMED))
    }

    @Test
    fun `abort registration changes status to ABORTED`() {
        val subscription = testNewsletterSubscription(status = SubscriptionStatus.PENDING)
        val aborted = subscription.abortRegistration()
        assertThat(aborted).isEqualTo(subscription.copy(status = SubscriptionStatus.ABORTED))
    }
}
