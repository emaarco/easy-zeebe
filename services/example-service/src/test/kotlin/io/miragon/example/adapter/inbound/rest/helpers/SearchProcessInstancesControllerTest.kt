package io.miragon.example.adapter.inbound.rest.helpers

import com.ninjasquad.springmockk.MockkBean
import io.miragon.common.zeebe.engine.ProcessEngineApi
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(SearchProcessInstancesController::class)
class SearchProcessInstancesControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var processEngineApi: ProcessEngineApi

    @Test
    fun `user retrieves all process instances`() {

        // given: mocked engine returning empty list & rest-operation
        every { processEngineApi.searchForProcessInstances() } returns emptyList()
        val operation = get("/api/process-instances")

        // when: request is performed
        val response = mockMvc.perform(operation).andReturn()

        // then: assert that the process engine was called & response is 200 with an empty list
        assertThat(response.response.status).isEqualTo(200)
        assertThat(response.response.contentAsString).isEqualTo("[]")
        verify { processEngineApi.searchForProcessInstances() }
        confirmVerified(processEngineApi)
    }
}
