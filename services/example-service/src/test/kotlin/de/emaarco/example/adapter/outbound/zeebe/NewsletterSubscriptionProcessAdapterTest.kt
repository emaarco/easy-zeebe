package de.emaarco.example.adapter.outbound.zeebe

import de.emaarco.common.zeebe.engine.ProcessEngineApi
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi
import de.emaarco.example.domain.SubscriptionId
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class NewsletterSubscriptionProcessAdapterTest {

    private val engineApi = mockk<ProcessEngineApi>()
    private val underTest = NewsletterSubscriptionProcessAdapter(engineApi = engineApi)

    @Test
    fun `starts newsletter subscription process`() {

        // Given
        val subscriptionId = SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val expectedProcessInstanceKey = 42L
        val expectedVariables = mapOf("subscriptionId" to subscriptionId.value.toString())
        every { engineApi.startProcess(any(), any()) } returns expectedProcessInstanceKey

        // When
        val result = underTest.submitForm(subscriptionId)

        // Then
        assertThat(result).isEqualTo(expectedProcessInstanceKey)
        verify {
            engineApi.startProcess(
                processId = NewsletterSubscriptionProcessApi.PROCESS_ID,
                variables = expectedVariables
            )
        }
    }

    @Test
    fun `confirmSubscription sends message with correct parameters`() {

        // Given
        val subscriptionId = SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        every { engineApi.sendMessage(any(), any(), any()) } just Runs

        // When
        underTest.confirmSubscription(subscriptionId)

        // Then
        verify {
            engineApi.sendMessage(
                messageName = NewsletterSubscriptionProcessApi.Messages.MESSAGE_SUBSCRIPTION_CONFIRMED,
                correlationId = subscriptionId.value.toString(),
                variables = emptyMap()
            )
        }
    }
}
