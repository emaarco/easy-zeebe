package de.emaarco.example.adapter.inbound.rest

import de.emaarco.common.zeebe.engine.ProcessEngineApi
import io.camunda.client.api.search.response.ProcessInstance
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/process-instances")
class ProcessInstanceController(private val processEngineApi: ProcessEngineApi) {

    private val log = KotlinLogging.logger {}

    @GetMapping
    fun getAllProcessInstances(): ResponseEntity<List<ProcessInstance>> {
        log.debug { "Received REST-request to query process instances" }
        val instances = processEngineApi.searchProcessInstances()
        return ResponseEntity.ok().body(instances)
    }

}
