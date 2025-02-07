package de.emaarco.common.test.zeebe.setup

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.worker.JobWorker

class JobWorkerManager(private val client: ZeebeClient) {

    private val registeredWorkers = mutableListOf<JobWorker>()

    fun registerWorkers(jobWorkers: List<de.emaarco.common.zeebe.worker.DefaultJobWorker>) {
        jobWorkers.forEach {
            registeredWorkers += client
                .newWorker()
                .jobType(it.type)
                .handler(it)
                .open()
        }
    }

    fun closeWorkers() {
        registeredWorkers.forEach { it.close() }
        registeredWorkers.clear()
    }
}