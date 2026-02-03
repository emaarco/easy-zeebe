package io.miragon.example.application.service

import io.miragon.example.application.port.inbound.SubscribeToNewsletterUseCase
import io.miragon.example.application.port.outbound.NewsletterSubscriptionProcess
import io.miragon.example.application.port.outbound.NewsletterSubscriptionRepository
import io.miragon.example.domain.Email
import io.miragon.example.domain.Name
import io.miragon.example.domain.NewsletterId
import io.miragon.example.domain.NewsletterSubscription
import io.miragon.example.domain.SubscriptionId
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class SubscribeToNewsletterServiceTest {

    private val processPort = mockk<NewsletterSubscriptionProcess>()
    private val subscriptionRepository = mockk<NewsletterSubscriptionRepository>()
    private val underTest = SubscribeToNewsletterService(
        repository = subscriptionRepository,
        processPort = processPort
    )

    @Test
    fun `create subscription`() {

        val captor = slot<NewsletterSubscription>()
        val newsletterId = UUID.fromString("f51d9793-1b24-45db-bd6f-dd4cb26795e6")
        every { subscriptionRepository.save(capture(captor)) } just Runs
        every { processPort.submitForm(any<SubscriptionId>()) } returns 1L

        val command = SubscribeToNewsletterUseCase.Command(
            newsletterId = NewsletterId(newsletterId),
            name = Name("John Doe"),
            email = Email("john.doe@test.com")
        )

        val subscription = underTest.subscribe(command)

        assertThat(subscription).isNotNull
        verify { processPort.submitForm(subscription) }
        verify { subscriptionRepository.save(captor.captured) }
        confirmVerified(processPort, subscriptionRepository)
    }

}