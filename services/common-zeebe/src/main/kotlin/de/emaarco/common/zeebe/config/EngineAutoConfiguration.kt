package de.emaarco.common.zeebe.config

import de.emaarco.common.zeebe.engine.ProcessEngineConfiguration
import io.camunda.zeebe.spring.client.annotation.Deployment
import org.springframework.context.annotation.Import

@Deployment(resources = ["classpath:bpmn/*.bpmn"])
@Import(ZeebeEnvironmentConfiguration::class, ProcessEngineConfiguration::class)
class EngineAutoConfiguration