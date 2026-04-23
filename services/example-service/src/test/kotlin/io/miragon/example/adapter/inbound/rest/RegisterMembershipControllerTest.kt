package io.miragon.example.adapter.inbound.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.miragon.example.application.port.inbound.RegisterMembershipUseCase
import io.miragon.example.domain.Age
import io.miragon.example.domain.Email
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.Name
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebMvcTest(RegisterMembershipController::class)
class RegisterMembershipControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var useCase: RegisterMembershipUseCase

    private val mapper = ObjectMapper()

    @Test
    fun `user registers for MiraVelo membership`() {

        // given: valid input data & rest-operation
        val userName = "Test User"
        val email = "test@example.com"
        val age = 28
        val input = mapOf("email" to email, "name" to userName, "age" to age)
        val membershipId = MembershipId("123e4567-e89b-12d3-a456-426614174000")
        val expectedCommand = RegisterMembershipUseCase.Command(
            email = Email("test@example.com"),
            name = Name("Test User"),
            age = Age(28),
        )

        every { useCase.register(any()) } returns membershipId

        val operation = post("/api/memberships")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(input))

        // when: request is performed
        val response = mockMvc.perform(operation).andReturn()

        // then: assert that a use-case was called & response contains membershipId
        val expectedResponse = mapOf("membershipId" to membershipId.value)
        assertThat(response.response.status).isEqualTo(200)
        assertThat(response.response.contentAsString).isEqualTo(mapper.writeValueAsString(expectedResponse))
        verify { useCase.register(expectedCommand) }
        confirmVerified(useCase)
    }
}
