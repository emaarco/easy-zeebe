package io.miragon.example.adapter.outbound.zeebe

import io.camunda.client.CamundaClient
import io.camunda.client.api.CamundaFuture
import io.camunda.client.api.command.CompleteUserTaskCommandStep1
import io.camunda.client.api.response.CompleteUserTaskResponse
import io.camunda.client.api.search.enums.UserTaskState
import io.camunda.client.api.search.filter.UserTaskFilter
import io.camunda.client.api.search.request.UserTaskSearchRequest
import io.camunda.client.api.search.response.SearchResponse
import io.camunda.client.api.search.response.UserTask
import io.github.emaarco.bpmn.runtime.VariableName
import io.miragon.common.zeebe.engine.ProcessEngineApi
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi
import io.miragon.example.domain.MembershipId
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*
import java.util.function.Consumer

class MembershipProcessAdapterTest {

    private val engineApi = mockk<ProcessEngineApi>()
    private val camundaClient = mockk<CamundaClient>()
    private val underTest = MembershipProcessAdapter(
        engineApi = engineApi,
        camundaClient = camundaClient,
    )

    @Test
    fun `registerMembership publishes the membership-requested message`() {

        // Given
        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val expectedVariables: Map<VariableName, Any> = mapOf(MiraveloMembershipProcessApi.Variables.StartEventMembershipRequested.MEMBERSHIP_ID to membershipId.value.toString())
        every { engineApi.sendMessage(any(), any(), any()) } just Runs

        // When
        underTest.registerMembership(membershipId)

        // Then
        verify {
            engineApi.sendMessage(
                messageName = MiraveloMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_REQUESTED,
                correlationId = membershipId.value.toString(),
                variables = expectedVariables,
            )
        }
        confirmVerified(engineApi)
    }

    @Test
    fun `confirmMembership completes the active user task for the member`() {

        // Given: a single CREATED user task matches the filter
        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val expectedUserTaskKey = 99L
        val filterSlot = slot<Consumer<UserTaskFilter>>()
        val capturedFilter = mockk<UserTaskFilter>(relaxed = true)
        val userTask = mockk<UserTask>()
        every { userTask.userTaskKey } returns expectedUserTaskKey

        val searchResponse = mockk<SearchResponse<UserTask>> { every { items() } returns listOf(userTask) }
        val searchFuture = mockk<CamundaFuture<SearchResponse<UserTask>>> { every { join() } returns searchResponse }
        val searchRequest = mockk<UserTaskSearchRequest>()
        every { searchRequest.filter(capture(filterSlot)) } answers {
            filterSlot.captured.accept(capturedFilter)
            searchRequest
        }
        every { searchRequest.send() } returns searchFuture
        every { camundaClient.newUserTaskSearchRequest() } returns searchRequest

        val completeFuture = mockk<CamundaFuture<CompleteUserTaskResponse>> { every { join() } returns mockk() }
        val completeCommand = mockk<CompleteUserTaskCommandStep1> { every { send() } returns completeFuture }
        every { camundaClient.newCompleteUserTaskCommand(expectedUserTaskKey) } returns completeCommand

        // When
        underTest.confirmMembership(membershipId)

        // Then: the adapter applied the expected filter and completed the task it found
        verify { capturedFilter.state(UserTaskState.CREATED) }
        verify { capturedFilter.elementId(MiraveloMembershipProcessApi.Elements.USER_TASK_CONFIRM_MEMBERSHIP.value) }
        verify {
            capturedFilter.processInstanceVariables(
                mapOf(MiraveloMembershipProcessApi.Variables.StartEventMembershipRequested.MEMBERSHIP_ID.value to membershipId.value.toString())
            )
        }
        verify { camundaClient.newCompleteUserTaskCommand(expectedUserTaskKey) }
        verify { completeCommand.send() }
    }

    @Test
    fun `rejectConfirmation publishes the confirmation-rejected message`() {

        // Given
        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        every { engineApi.sendMessage(any(), any(), any()) } just Runs

        // When
        underTest.rejectConfirmation(membershipId)

        // Then: message + correlation key are pinned; variables are not part of the contract
        verify {
            engineApi.sendMessage(
                messageName = MiraveloMembershipProcessApi.Messages.MIRAVELO_CONFIRMATION_REJECTED,
                correlationId = membershipId.value.toString(),
                variables = any(),
            )
        }
        confirmVerified(engineApi)
    }
}
