package io.miragon.example.application.service

import io.miragon.example.application.port.outbound.MembershipProcess
import io.miragon.example.application.port.outbound.MembershipRepository
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.testMembership
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class ConfirmMembershipServiceTest {

    private val membershipRepository = mockk<MembershipRepository>()
    private val processPort = mockk<MembershipProcess>()
    private val underTest = ConfirmMembershipService(
        repository = membershipRepository,
        processPort = processPort
    )

    @Test
    fun `confirm membership`() {

        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val membership = testMembership(id = membershipId)

        every { membershipRepository.find(membershipId) } returns membership
        every { membershipRepository.save(membership) } just Runs
        every { processPort.confirmMembership(membershipId) } just Runs

        underTest.confirm(membershipId)

        verify { membershipRepository.find(membershipId) }
        verify { membershipRepository.save(membership) }
        verify { processPort.confirmMembership(membershipId) }
        confirmVerified(membershipRepository, processPort)
    }
}
