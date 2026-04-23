package io.miragon.example.adapter.inbound.rest

import com.ninjasquad.springmockk.MockkBean
import io.miragon.example.application.port.inbound.RejectConfirmationUseCase
import io.miragon.example.domain.MembershipId
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebMvcTest(RejectConfirmationController::class)
class RejectConfirmationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var useCase: RejectConfirmationUseCase

    @Test
    fun `user rejects confirmation`() {

        // given: valid path variable & rest-operation
        val pathVar = "123e4567-e89b-12d3-a456-426614174000"
        every { useCase.rejectConfirmation(any()) } just Runs
        val operation = post("/api/memberships/reject-confirmation/{membershipId}", pathVar)

        // when: request is performed
        val response = mockMvc.perform(operation).andReturn()

        // then: assert that a use-case was called & response is 200
        assertThat(response.response.status).isEqualTo(200)
        verify { useCase.rejectConfirmation(MembershipId(pathVar)) }
        confirmVerified(useCase)
    }
}
