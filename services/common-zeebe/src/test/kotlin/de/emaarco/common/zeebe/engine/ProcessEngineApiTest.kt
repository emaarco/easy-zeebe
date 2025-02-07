package de.emaarco.common.zeebe.engine

import io.camunda.zeebe.client.ZeebeClient
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Duration

class ProcessEngineApiTest {

    private val zeebeClient = mockk<ZeebeClient>(relaxed = true)
    private val underTest = ProcessEngineApi(zeebeClient)

    @Test
    fun `should send message`() {

        val testVariables = mapOf("dummy" to "dummy")
        val correlationId = "correlationId"
        val messageName = "messageName"

        underTest.sendMessage(
            correlationId = correlationId,
            messageName = messageName,
            variables = testVariables
        )

        verify {
            zeebeClient.newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(correlationId)
                .variables(testVariables)
                .timeToLive(Duration.ofSeconds(10))
                .send().join()
        }
    }

    @Test
    fun `should send start process message`() {

        val testVariables = mapOf("dummy" to "dummy")
        val messageName = "messageName"

        underTest.startProcessViaMessage(
            messageName = messageName,
            variables = testVariables
        )

        verify {
            zeebeClient.newPublishMessageCommand()
                .messageName(messageName)
                .withoutCorrelationKey()
                .variables(testVariables)
                .send().join()
        }
    }

}