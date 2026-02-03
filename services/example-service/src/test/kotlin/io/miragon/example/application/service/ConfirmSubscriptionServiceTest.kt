package io.miragon.example.application.service

import io.miragon.example.application.port.outbound.NewsletterSubscriptionProcess
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

class ConfirmSubscriptionServiceTest {

    private val subscriptionRepository = mockk<NewsletterSubscriptionRepository>()
    private val processPort = mockk<NewsletterSubscriptionProcess>()
    private val underTest = ConfirmSubscriptionService(
        repository = subscriptionRepository,
        processPort = processPort
    )

    @Test
    fun `confirm subscription`() {

        val subscriptionId = SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val subscription = testNewsletterSubscription(id = subscriptionId)

        every { subscriptionRepository.find(subscriptionId) } returns subscription
        every { subscriptionRepository.save(subscription) } just Runs
        every { processPort.confirmSubscription(subscriptionId) } just Runs

        underTest.confirm(subscriptionId)

        verify { subscriptionRepository.find(subscriptionId) }
        verify { subscriptionRepository.save(subscription) }
        verify { processPort.confirmSubscription(subscriptionId) }
        confirmVerified(subscriptionRepository, processPort)
    }
}
