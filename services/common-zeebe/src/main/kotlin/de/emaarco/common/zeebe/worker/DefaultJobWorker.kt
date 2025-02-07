package de.emaarco.common.zeebe.worker

import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.JobClient
import io.camunda.zeebe.client.api.worker.JobHandler
import io.camunda.zeebe.spring.common.exception.ZeebeBpmnError
import mu.KotlinLogging

/**
 * Abstract base-class for handling zeebe-service-tasks.
 * It provides a template for executing specific service-tasks, identified by their type.
 * The workers are automatically registered in the JobWorkerManager.
 * @param type the type of the job to handle
 * @param customTimeout timeout for the job. if not provided the default timeout is used
 * @see JobWorkerManager
 */
abstract class DefaultJobWorker(
    val type: String,
    val customTimeout: Long? = null,
    private val autoComplete: Boolean = true
) : JobHandler {

    private val log = KotlinLogging.logger {}

    override fun handle(client: JobClient, job: ActivatedJob) {
        try {
            val result = executeTask(client, job)
            if (autoComplete) completeJob(client, job, result)
        } catch (e: ZeebeBpmnError) {
            log.error(e) { "Error while processing Zeebe Job" }
            client.newThrowErrorCommand(job.key).errorCode(e.errorCode).errorMessage(e.errorMessage).send().join()
        } catch (e: Exception) {
            log.error(e) { "Error while processing Zeebe Job" }
            client.newFailCommand(job.key).retries(job.retries - 1).errorMessage(e.message).send().join()
            throw e
        }
    }

    abstract fun executeTask(client: JobClient, job: ActivatedJob): Map<String, Any?>

    private fun completeJob(client: JobClient, job: ActivatedJob, result: Map<String, Any?>) {
        client.newCompleteCommand(job.key).variables(result).send().join()
    }

}
