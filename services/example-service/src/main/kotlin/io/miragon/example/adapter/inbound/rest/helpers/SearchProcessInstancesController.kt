package io.miragon.example.adapter.inbound.rest.helpers

import io.miragon.common.zeebe.engine.ProcessEngineApi
import io.camunda.client.api.search.response.ProcessInstance
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Not part of the use-case.
 * Exists to demonstrate process-engine query implementation and test responses.
 */
@RestController
@RequestMapping("/api/process-instances")
class SearchProcessInstancesController(
    private val processEngineApi: ProcessEngineApi
) {

    private val log = KotlinLogging.logger {}

    @GetMapping
    fun getAllProcessInstances(): ResponseEntity<List<ProcessInstance>> {
        log.debug { "Received REST-request to query process instances" }
        val instances = processEngineApi.searchForProcessInstances()
        return ResponseEntity.ok().body(instances)
    }

}