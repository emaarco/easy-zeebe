package de.emaarco.example.application.service

import de.emaarco.example.application.port.outbound.NewsletterSubscriptionRepository
import de.emaarco.example.domain.Email
import de.emaarco.example.domain.Name
import de.emaarco.example.domain.NewsletterId
import de.emaarco.example.domain.NewsletterSubscription
import de.emaarco.example.domain.SubscriptionId
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
        val subscription = NewsletterSubscription(
            id = subscriptionId,
            name = Name("John Doe"),
            email = Email("john.doe@test.com"),
            newsletter = NewsletterId(UUID.randomUUID())
        )

        every { subscriptionRepository.find(subscriptionId) } returns subscription

        underTest.sendConfirmationMail(subscriptionId)

        verify { subscriptionRepository.find(subscriptionId) }
        confirmVerified(subscriptionRepository)
    }
}
