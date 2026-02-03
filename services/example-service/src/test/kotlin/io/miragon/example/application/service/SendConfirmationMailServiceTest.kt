package io.miragon.example.application.service

import io.miragon.example.application.port.outbound.NewsletterSubscriptionRepository
import io.miragon.example.domain.SubscriptionId
import io.miragon.example.domain.testNewsletterSubscription
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class SendConfirmationMailServiceTest {

    private val subscriptionRepository = mockk<NewsletterSubscriptionRepository>()
    private val underTest = SendConfirmationMailService(repository = subscriptionRepository)

    @Test
    fun `send confirmation mail`() {

        val subscriptionId = SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val subscription = testNewsletterSubscription(id = subscriptionId)

        every { subscriptionRepository.find(subscriptionId) } returns subscription

        underTest.sendConfirmationMail(subscriptionId)

        verify { subscriptionRepository.find(subscriptionId) }
        confirmVerified(subscriptionRepository)
    }
}
