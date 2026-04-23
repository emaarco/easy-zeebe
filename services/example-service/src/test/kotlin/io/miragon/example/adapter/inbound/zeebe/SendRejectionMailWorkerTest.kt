package io.miragon.example.adapter.inbound.zeebe

import io.miragon.example.application.port.inbound.SendRejectionMailUseCase
import io.miragon.example.domain.MembershipId
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class SendRejectionMailWorkerTest {

    private val useCase = mockk<SendRejectionMailUseCase>()
    private val underTest = SendRejectionMailWorker(useCase)

    @Test
    fun `should send rejection mail when job is received`() {

        // Given: a membership and mocked service-calls
        val membershipId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        every { useCase.sendRejectionMail(MembershipId(membershipId)) } just Runs

        // When: the worker handles the job
        underTest.handle(membershipId)

        // Then: the use case is called with the correct membership ID
        verify(exactly = 1) { useCase.sendRejectionMail(MembershipId(membershipId)) }
        confirmVerified(useCase)
    }
}
