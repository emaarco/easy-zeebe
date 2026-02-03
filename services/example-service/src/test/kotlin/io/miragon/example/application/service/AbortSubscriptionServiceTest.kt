package io.miragon.example.application.service

import io.miragon.example.application.port.outbound.NewsletterSubscriptionRepository
import io.miragon.example.domain.SubscriptionId
import io.miragon.example.domain.testNewsletterSubscription
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class AbortSubscriptionServiceTest {

    private val subscriptionRepository = mockk<NewsletterSubscriptionRepository>()
    private val underTest = AbortSubscriptionService(repository = subscriptionRepository)

    @Test
    fun `abort subscription`() {

        val subscriptionId = SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val subscription = testNewsletterSubscription(id = subscriptionId)

        every { subscriptionRepository.find(subscriptionId) } returns subscription
        every { subscriptionRepository.save(subscription) } just Runs

        underTest.abort(subscriptionId)

        verify { subscriptionRepository.find(subscriptionId) }
        verify { subscriptionRepository.save(subscription) }
        confirmVerified(subscriptionRepository)
    }
}
