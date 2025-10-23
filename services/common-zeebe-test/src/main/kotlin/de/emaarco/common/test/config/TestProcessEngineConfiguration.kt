package de.emaarco.common.test.config

import de.emaarco.common.zeebe.engine.ProcessEngineApi
import io.camunda.client.CamundaClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * Test configuration that ensures ProcessEngineApi uses the test CamundaClient
 * provided by @CamundaProcessTest instead of the production client.
 */
@TestConfiguration
class TestProcessEngineConfiguration {

    @Bean
    @Primary
    @ConditionalOnBean(CamundaClient::class)
    fun testProcessEngineApi(camundaClient: CamundaClient): ProcessEngineApi {
        return ProcessEngineApi(camundaClient)
    }
}
