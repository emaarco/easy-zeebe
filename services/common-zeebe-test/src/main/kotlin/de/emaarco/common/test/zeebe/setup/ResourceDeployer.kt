package de.emaarco.common.test.zeebe.setup

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1
import io.camunda.zeebe.model.bpmn.Bpmn
import io.camunda.zeebe.process.test.assertions.BpmnAssert

/**
 * This class is responsible for deploying processes & decisions to the test-engine.
 */
class ResourceDeployer(private val client: ZeebeClient) {

    fun deployResources(processes: List<String>, decisions: List<String>) {
        val resources = processes + decisions
        val commandStep1 = client.newDeployResourceCommand()
        var commandStep2: DeployResourceCommandStep1.DeployResourceCommandStep2? = null
        for (process in resources) {
            commandStep2 = if (commandStep2 == null) {
                commandStep1.addResourceFromClasspath(process)
            } else {
                commandStep2.addResourceFromClasspath(process)
            }
        }
        val result = commandStep2!!.send().join()
        BpmnAssert.assertThat(result).containsProcessesByResourceName(*processes.toTypedArray())
    }

    /**
     * Can be used to mock a specific child process that is called by a main process.
     * This is useful, if you just want to test the main process.
     */
    fun mockChildProcess(processId: String) {
        val model = Bpmn.createExecutableProcess(processId).startEvent().done()
        client.newDeployResourceCommand()
            .addProcessModel(model, "$processId.bpmn")
            .send()
            .join()
    }
}