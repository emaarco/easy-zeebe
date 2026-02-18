package io.miragon.example.adapter.outbound.zeebe

import io.miragon.common.zeebe.engine.ProcessEngineApi
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi
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

class NewsletterSubscriptionProcessAdapterTest {

    private val engineApi = mockk<ProcessEngineApi>()
    private val underTest = NewsletterSubscriptionProcessAdapter(engineApi = engineApi)

    @Test
    fun `starts newsletter subscription process`() {

        // Given
        val subscriptionId = SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val expectedProcessInstanceKey = 42L
        val expectedVariables = mapOf(NewsletterSubscriptionProcessApi.Variables.SUBSCRIPTION_ID to subscriptionId.value.toString())
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
        confirmVerified(engineApi)
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
                messageName = NewsletterSubscriptionProcessApi.Messages.NEWSLETTER_SUBSCRIPTION_CONFIRMED,
                correlationId = subscriptionId.value.toString(),
                variables = emptyMap()
            )
        }
        confirmVerified(engineApi)
    }
}
