package de.emaarco.example.adapter.inbound.zeebe

import de.emaarco.example.application.port.inbound.SendConfirmationMailUseCase
import de.emaarco.example.domain.SubscriptionId
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Unit test for SendConfirmationMailWorker.
 * Tests that the worker correctly extracts variables and calls the use case.
 */
class SendConfirmationMailWorkerTest {

    private val useCase = mockk<SendConfirmationMailUseCase>()
    private val underTest = SendConfirmationMailWorker(useCase)

    @Test
    fun `should send confirmation mail when job is received`() {

        // Given
        val subscriptionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        every { useCase.sendConfirmationMail(SubscriptionId(subscriptionId)) } just Runs

        // When
        underTest.handle(subscriptionId)

        // Then
        verify(exactly = 1) { useCase.sendConfirmationMail(SubscriptionId(subscriptionId)) }
        confirmVerified(useCase)
    }
}
