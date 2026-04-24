package io.miragon.common.zeebe.engine

import io.camunda.client.CamundaClient
import io.camunda.client.api.response.ProcessInstanceEvent
import io.github.emaarco.bpmn.runtime.MessageName
import io.github.emaarco.bpmn.runtime.ProcessId
import io.github.emaarco.bpmn.runtime.VariableName
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

        val dummyVar = VariableName.Output("dummy")
        val testVariables: Map<VariableName, Any> = mapOf(dummyVar to "dummy")
        val correlationId = "correlationId"
        val messageName = MessageName("messageName")

        underTest.sendMessage(
            correlationId = correlationId,
            messageName = messageName,
            variables = testVariables
        )

        verify {
            camundaClient.newPublishMessageCommand()
                .messageName(messageName.value)
                .correlationKey(correlationId)
                .variables(mapOf(dummyVar.value to "dummy"))
                .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
                .send().join()
        }
    }

    @Test
    fun `should send start process message`() {

        // given: mock the process instance creation
        val dummyVar = VariableName.Output("dummy")
        val testVariables: Map<VariableName, Any> = mapOf(dummyVar to "dummy")
        val processId = ProcessId("my-process")
        val expectedKey = 12345L
        val instanceEvent = mockk<ProcessInstanceEvent>()
        val plainCommand = camundaClient.newCreateInstanceCommand().bpmnProcessId(processId.value).latestVersion()
        every { instanceEvent.processInstanceKey } returns expectedKey
        every { plainCommand.variables(mapOf(dummyVar.value to "dummy")).send().join() } returns instanceEvent

        // when: start the process
        val result = underTest.startProcess(processId = processId, variables = testVariables)

        // then: the process instance key should be returned
        assertThat(result).isEqualTo(expectedKey)
        verify { plainCommand.variables(mapOf(dummyVar.value to "dummy")).send().join() }
    }

}