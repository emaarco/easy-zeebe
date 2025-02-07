package de.emaarco.common.zeebe.worker

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.worker.JobWorker
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import mu.KotlinLogging

/**
 * Manages all job-workers for a specific service.
 * Ensures that the workers are registered and tasks are executed.
 * Automatically registers workers at startup and shuts them down gracefully.
 */
class JobWorkerManager(
    private val zeebeClient: ZeebeClient,
    private val jobWorkers: List<de.emaarco.common.zeebe.worker.DefaultJobWorker>,
) {

    private val log = KotlinLogging.logger {}
    private val workers = mutableListOf<JobWorker>()

    @PostConstruct
    fun register() = jobWorkers.forEach {
        log.info { "Registering worker for job type '${it.type}'" }
        val worker = buildWorker(it)
        workers.add(worker)
    }

    @PreDestroy
    fun shutdown() = workers.forEach {
        log.info { "Closing worker: $it" }
        it.close()
    }

    private fun buildWorker(worker: de.emaarco.common.zeebe.worker.DefaultJobWorker) = zeebeClient
        .newWorker()
        .jobType(worker.type)
        .handler(worker)
        .timeout(worker.customTimeout ?: (5 * 60 * 1000))
        .open()

}