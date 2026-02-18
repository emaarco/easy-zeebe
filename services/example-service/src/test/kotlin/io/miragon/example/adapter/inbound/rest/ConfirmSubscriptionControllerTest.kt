package io.miragon.example.adapter.inbound.rest

import com.ninjasquad.springmockk.MockkBean
import io.miragon.example.application.port.inbound.ConfirmSubscriptionUseCase
import io.miragon.example.domain.SubscriptionId
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebMvcTest(ConfirmSubscriptionController::class)
class ConfirmSubscriptionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var useCase: ConfirmSubscriptionUseCase

    @Test
    fun `user confirms subscription`() {

        // given: valid path variable & rest-operation
        val pathVar = "123e4567-e89b-12d3-a456-426614174000"
        every { useCase.confirm(any()) } just Runs
        val operation = post("/api/subscriptions/confirm/{subscriptionId}", pathVar)

        // when: request is performed
        val response = mockMvc.perform(operation).andReturn()

        // then: assert that a use-case was called & response is 200
        assertThat(response.response.status).isEqualTo(200)
        verify { useCase.confirm(SubscriptionId(pathVar)) }
        confirmVerified(useCase)
    }
}
