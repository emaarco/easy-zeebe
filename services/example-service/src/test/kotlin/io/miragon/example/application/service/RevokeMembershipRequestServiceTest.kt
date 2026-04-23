package io.miragon.example.application.service

import io.miragon.example.application.port.outbound.MembershipRepository
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.MembershipStatus
import io.miragon.example.domain.testMembership
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class RevokeMembershipRequestServiceTest {

    private val membershipRepository = mockk<MembershipRepository>()
    private val underTest = RevokeMembershipRequestService(repository = membershipRepository)

    @Test
    fun `revoke membership request persists declined status`() {

        // given: a pending membership in the repository
        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val membership = testMembership(id = membershipId)
        val declined = membership.declineMembership()
        every { membershipRepository.find(membershipId) } returns membership
        every { membershipRepository.save(declined) } just Runs

        // when: the use case is invoked
        underTest.revokeMembershipRequest(membershipId)

        // then: the membership is loaded and persisted with DECLINED status
        verify { membershipRepository.find(membershipId) }
        verify { membershipRepository.save(match { it.status == MembershipStatus.DECLINED }) }
        confirmVerified(membershipRepository)
    }
}
