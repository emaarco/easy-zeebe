package de.emaarco.example.application.service

import de.emaarco.example.application.port.outbound.NewsletterSubscriptionRepository
import de.emaarco.example.domain.Email
import de.emaarco.example.domain.Name
import de.emaarco.example.domain.NewsletterId
import de.emaarco.example.domain.NewsletterSubscription
import de.emaarco.example.domain.SubscriptionId
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
        val subscription = NewsletterSubscription(
            id = subscriptionId,
            name = Name("John Doe"),
            email = Email("john.doe@test.com"),
            newsletter = NewsletterId(UUID.randomUUID())
        )

        every { subscriptionRepository.find(subscriptionId) } returns subscription
        every { subscriptionRepository.save(subscription) } just Runs

        underTest.abort(subscriptionId)

        verify { subscriptionRepository.find(subscriptionId) }
        verify { subscriptionRepository.save(subscription) }
        confirmVerified(subscriptionRepository)
    }
}
