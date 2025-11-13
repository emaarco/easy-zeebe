package de.emaarco.example.application.service

import de.emaarco.example.application.port.outbound.NewsletterSubscriptionRepository
import de.emaarco.example.domain.SubscriptionId
import de.emaarco.example.domain.testNewsletterSubscription
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class SendWelcomeMailServiceTest {

    private val subscriptionRepository = mockk<NewsletterSubscriptionRepository>()
    private val underTest = SendWelcomeMailService(repository = subscriptionRepository)

    @Test
    fun `send welcome mail`() {

        val subscriptionId = SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val subscription = testNewsletterSubscription(id = subscriptionId)

        every { subscriptionRepository.find(subscriptionId) } returns subscription

        underTest.sendWelcomeMail(subscriptionId)

        verify { subscriptionRepository.find(subscriptionId) }
        confirmVerified(subscriptionRepository)
    }
}
