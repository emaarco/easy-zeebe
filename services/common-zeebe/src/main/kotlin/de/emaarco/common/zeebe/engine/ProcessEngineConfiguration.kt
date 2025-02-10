package de.emaarco.common.zeebe.engine

import de.emaarco.common.zeebe.worker.DefaultJobWorker
import de.emaarco.common.zeebe.worker.JobWorkerManager
import io.camunda.zeebe.client.ZeebeClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProcessEngineConfiguration {

    @Bean
    fun zeebeOutAdapter(zeebeClient: ZeebeClient) = ProcessEngineApi(
        zeebeClient = zeebeClient,
    )

    @Bean
    fun customJobWorkerManager(
        zeebeClient: ZeebeClient,
        jobWorkers: List<DefaultJobWorker>
    ) = JobWorkerManager(
        zeebeClient = zeebeClient,
        jobWorkers = jobWorkers
    )

}