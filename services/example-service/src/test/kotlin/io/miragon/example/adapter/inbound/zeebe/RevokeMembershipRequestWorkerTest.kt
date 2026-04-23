package io.miragon.example.adapter.inbound.zeebe

import io.miragon.example.application.port.inbound.RevokeMembershipRequestUseCase
import io.miragon.example.domain.MembershipId
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class RevokeMembershipRequestWorkerTest {

    private val useCase = mockk<RevokeMembershipRequestUseCase>()
    private val underTest = RevokeMembershipRequestWorker(useCase)

    @Test
    fun `should revoke membership request when job is received`() {

        // Given: a membership and mocked service-calls
        val membershipId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        every { useCase.revokeMembershipRequest(MembershipId(membershipId)) } just Runs

        // When: the worker handles the job
        underTest.handle(membershipId)

        // Then: the use case is called with the correct membership ID
        verify(exactly = 1) { useCase.revokeMembershipRequest(MembershipId(membershipId)) }
        confirmVerified(useCase)
    }
}
