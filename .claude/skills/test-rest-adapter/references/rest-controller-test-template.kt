package io.miragon.example.adapter.inbound.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.miragon.example.application.port.inbound.SubscribeToNewsletterUseCase
import io.miragon.example.domain.Email
import io.miragon.example.domain.Name
import io.miragon.example.domain.NewsletterId
import io.miragon.example.domain.SubscriptionId
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

// --- POST with request body ---
// @WebMvcTest loads only the web slice. Use @MockkBean for every use-case dependency.
// Stub with any() in every { }; verify the exact command object in verify { }.
@WebMvcTest(SubscribeToNewsletterController::class)
class SubscribeToNewsletterControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var useCase: SubscribeToNewsletterUseCase

    private val mapper = ObjectMapper()

    @Test
    fun `user subscribes to newsletter`() {

        // given: valid input data & rest-operation
        val userName = "Test User"
        val email = "test@example.com"
        val newsletterId = "9aed646c-b92a-4163-9f59-a0cfcbb66a43"
        val input = mapOf("email" to email, "name" to userName, "newsletterId" to newsletterId)
        val subscriptionId = SubscriptionId("123e4567-e89b-12d3-a456-426614174000")
        val expectedCommand = SubscribeToNewsletterUseCase.Command(
            newsletterId = NewsletterId("9aed646c-b92a-4163-9f59-a0cfcbb66a43"),
            email = Email("test@example.com"),
            name = Name("Test User")
        )

        every { useCase.subscribe(any()) } returns subscriptionId

        val operation = post("/api/subscriptions/subscribe")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(input))

        // when: request is performed
        val response = mockMvc.perform(operation).andReturn()

        // then: assert that a use-case was called & response contains subscriptionId
        val expectedResponse = mapOf("subscriptionId" to subscriptionId.value)
        assertThat(response.response.status).isEqualTo(200)
        assertThat(response.response.contentAsString).isEqualTo(mapper.writeValueAsString(expectedResponse))
        verify { useCase.subscribe(expectedCommand) }
        confirmVerified(useCase)
    }
}

// --- POST/DELETE with path variable (void use case) ---
// No ObjectMapper needed. Stub with just Runs; verify exact domain ID object.
//
// @WebMvcTest(ConfirmSubscriptionController::class)
// class ConfirmSubscriptionControllerTest {
//
//     @Autowired
//     private lateinit var mockMvc: MockMvc
//
//     @MockkBean
//     private lateinit var useCase: ConfirmSubscriptionUseCase
//
//     @Test
//     fun `user confirms subscription`() {
//
//         // given: valid path variable & rest-operation
//         val pathVar = "123e4567-e89b-12d3-a456-426614174000"
//         every { useCase.confirm(any()) } just Runs
//         val operation = post("/api/subscriptions/confirm/{subscriptionId}", pathVar)
//
//         // when: request is performed
//         val response = mockMvc.perform(operation).andReturn()
//
//         // then: assert that a use-case was called & response is 200
//         assertThat(response.response.status).isEqualTo(200)
//         verify { useCase.confirm(SubscriptionId(pathVar)) }
//         confirmVerified(useCase)
//     }
// }
