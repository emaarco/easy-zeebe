package de.emaarco.common.zeebe.config

import de.emaarco.common.zeebe.engine.ProcessEngineApi
import io.camunda.client.CamundaClient
import io.camunda.client.annotation.Deployment
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Deployment(resources = ["classpath:bpmn/*.bpmn"])
@Import(ZeebeEnvironmentConfiguration::class)
class EngineAutoConfiguration {

    @Bean
    fun processEngineApi(
        camundaClient: CamundaClient,
    ) = ProcessEngineApi(
        camundaClient = camundaClient
    )

}