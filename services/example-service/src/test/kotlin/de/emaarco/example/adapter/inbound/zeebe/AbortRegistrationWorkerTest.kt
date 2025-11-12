package de.emaarco.example.adapter.inbound.zeebe

import de.emaarco.example.application.port.inbound.AbortSubscriptionUseCase
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
 * Unit test for AbortRegistrationWorker.
 * Tests that the worker correctly extracts variables and calls the use case.
 */
class AbortRegistrationWorkerTest {

    private val useCase = mockk<AbortSubscriptionUseCase>()
    private val underTest = AbortRegistrationWorker(useCase)

    @Test
    fun `should abort registration when job is received`() {

        // Given
        val subscriptionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        every { useCase.abort(SubscriptionId(subscriptionId)) } just Runs

        // When
        underTest.handle(subscriptionId)

        // Then
        verify(exactly = 1) { useCase.abort(SubscriptionId(subscriptionId)) }
        confirmVerified(useCase)
    }
}