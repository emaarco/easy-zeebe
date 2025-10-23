package de.emaarco.common.zeebe.engine

import io.camunda.client.CamundaClient
import io.camunda.client.api.response.ProcessInstanceEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.temporal.ChronoUnit

class ProcessEngineApiTest {

    private val camundaClient = mockk<CamundaClient>(relaxed = true)
    private val underTest = ProcessEngineApi(camundaClient)

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
            camundaClient.newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(correlationId)
                .variables(testVariables)
                .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
                .send().join()
        }
    }

    @Test
    fun `should send start process message`() {

        // given: mock the process instance creation
        val testVariables = mapOf("dummy" to "dummy")
        val processId = "my-process"
        val expectedKey = 12345L
        val instanceEvent = mockk<ProcessInstanceEvent>()
        val plainCommand = camundaClient.newCreateInstanceCommand().bpmnProcessId(processId).latestVersion()
        every { instanceEvent.processInstanceKey } returns expectedKey
        every { plainCommand.variables(testVariables).send().join() } returns instanceEvent

        // when: start the process
        val result = underTest.startProcess(processId = processId, variables = testVariables)

        // then: the process instance key should be returned
        assertThat(result).isEqualTo(expectedKey)
        verify { plainCommand.variables(testVariables).send().join() }
    }

}