package io.miragon.example.adapter.inbound.zeebe

import io.miragon.example.application.port.inbound.SendWelcomeMailUseCase
import io.miragon.example.domain.SubscriptionId
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Unit test for SendWelcomeMailWorker.
 * Tests that the worker correctly extracts variables and calls the use case.
 */
class SendWelcomeMailWorkerTest {

    private val useCase = mockk<SendWelcomeMailUseCase>()
    private val underTest = SendWelcomeMailWorker(useCase)

    @Test
    fun `should send welcome mail when job is received`() {

        // Given
        val subscriptionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        every { useCase.sendWelcomeMail(SubscriptionId(subscriptionId)) } just Runs

        // When
        underTest.handle(subscriptionId)

        // Then
        verify(exactly = 1) { useCase.sendWelcomeMail(SubscriptionId(subscriptionId)) }
        confirmVerified(useCase)
    }
}
