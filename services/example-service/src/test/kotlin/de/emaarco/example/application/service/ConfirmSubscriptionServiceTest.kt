package de.emaarco.example.application.service

import de.emaarco.example.application.port.outbound.NewsletterSubscriptionProcess
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
        val subscription = NewsletterSubscription(
            id = subscriptionId,
            name = Name("John Doe"),
            email = Email("john.doe@test.com"),
            newsletter = NewsletterId(UUID.randomUUID())
        )

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
