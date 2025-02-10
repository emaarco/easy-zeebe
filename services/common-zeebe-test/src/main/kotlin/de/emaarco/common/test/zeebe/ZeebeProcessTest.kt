package de.emaarco.common.test.zeebe

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.emaarco.common.test.zeebe.setup.JobWorkerManager
import de.emaarco.common.test.zeebe.setup.ResourceDeployer
import de.emaarco.common.zeebe.worker.DefaultJobWorker
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.process.test.api.ZeebeTestEngine
import io.camunda.zeebe.process.test.assertions.BpmnAssert
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest
import io.camunda.zeebe.process.test.filters.RecordStream
import io.camunda.zeebe.process.test.inspections.InspectionUtility
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.time.Duration

/**
 * Base class for all zeebe-process-tests
 * @param processes list of bpmn files to deploy
 * @param decisions list of dmn files to deploy
 */
@ZeebeProcessTest
abstract class ZeebeProcessTest(
    private val processes: List<String>,
    private val decisions: List<String> = emptyList()
) {

    private val resourceDeployer by lazy { ResourceDeployer(client) }
    private val workerManager by lazy { JobWorkerManager(client) }

    protected lateinit var client: ZeebeClient
    protected lateinit var engine: ZeebeTestEngine
    private lateinit var recordStream: RecordStream

    abstract val jobWorkers: List<DefaultJobWorker>
    abstract fun initProcessPort(client: ZeebeClient)
    abstract fun beforeEach()
    open fun afterEach() = Unit
    open val childProcessMocks: List<String> = emptyList()

    @BeforeEach
    fun beforeEveryTest() {
        client = engine.createClient(jacksonObjectMapper().registerModule(JavaTimeModule()))
        recordStream = RecordStream.of(engine.recordStreamSource)
        initProcessPort(client)
        resourceDeployer.deployResources(processes, decisions)
        childProcessMocks.forEach { resourceDeployer.mockChildProcess(it) }
        workerManager.registerWorkers(jobWorkers)
        beforeEach()
    }

    @AfterEach
    fun afterEveryTest() {
        afterEach()
        recordStream.records().forEach { println(it) }
        workerManager.closeWorkers()
        client.close()
    }

    fun waitForProcessInstanceHasReachedElement(
        elementName: String
    ) = Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted {
        // Wait the engine to execute the worker
        Thread.sleep(30)
        BpmnAssert.initRecordStream(RecordStream.of(engine.recordStreamSource))
        BpmnAssert.assertThat(getProcessInstance()).isWaitingAtElements(elementName)
    }

    fun waitForProcessInstanceHasPassedElement(
        elementName: String,
        times: Int = 1
    ) = waitForProcessInstanceHasPassedElement(elementName, Duration.ofSeconds(5), times)

    fun waitForProcessInstanceHasPassedElement(
        elementName: String, duration: Duration, times: Int
    ) = Awaitility.await().atMost(duration).untilAsserted {
        // Wait the engine to execute the worker
        Thread.sleep(30)
        BpmnAssert.initRecordStream(RecordStream.of(engine.recordStreamSource))
        BpmnAssert.assertThat(getProcessInstance()).hasPassedElement(elementName, times)
    }

    fun increaseTime(duration: Duration) = TimerUtils.increaseTime(engine, duration)

    fun assertThatProcess(): ProcessInstanceAssert {
        return BpmnAssert.assertThat(getProcessInstance())
    }

    private fun getProcessInstance(): InspectedProcessInstance {
        var latestException: Exception? = null
        repeat(10) {
            try {
                return InspectionUtility.findProcessInstances().findFirstProcessInstance().orElseThrow()
            } catch (e: Exception) {
                latestException = e
                Thread.sleep(300)
            }
        }

        throw latestException!!
    }
}

