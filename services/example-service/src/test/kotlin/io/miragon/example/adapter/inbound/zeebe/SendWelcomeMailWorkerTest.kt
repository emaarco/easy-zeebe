package io.miragon.example.adapter.inbound.zeebe

import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Variables.WELCOME_MAIL_SENT
import io.miragon.example.application.port.inbound.SendWelcomeMailUseCase
import io.miragon.example.domain.SubscriptionId
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*
import java.util.Map.entry

class SendWelcomeMailWorkerTest {

    private val useCase = mockk<SendWelcomeMailUseCase>()
    private val underTest = SendWelcomeMailWorker(useCase)

    @Test
    fun `should send welcome mail when job is received`() {

        // Given: a subscription and mocked service-calls
        val subscriptionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        every { useCase.sendWelcomeMail(SubscriptionId(subscriptionId)) } just Runs

        // When: the worker handles the job
        val result = underTest.handle(subscriptionId)

        // Then: the use case is called with the correct subscription ID
        assertThat(result).containsExactly(entry(WELCOME_MAIL_SENT, true))
        verify(exactly = 1) { useCase.sendWelcomeMail(SubscriptionId(subscriptionId)) }
        confirmVerified(useCase)
    }
}
