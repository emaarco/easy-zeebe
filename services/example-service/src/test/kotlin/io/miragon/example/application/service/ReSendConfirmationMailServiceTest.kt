package io.miragon.example.application.service

import io.miragon.example.application.port.outbound.MembershipRepository
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.testMembership
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class ReSendConfirmationMailServiceTest {

    private val membershipRepository = mockk<MembershipRepository>()
    private val underTest = ReSendConfirmationMailService(repository = membershipRepository)

    @Test
    fun `re-send confirmation mail`() {

        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val membership = testMembership(id = membershipId)

        every { membershipRepository.find(membershipId) } returns membership

        underTest.reSendConfirmationMail(membershipId)

        verify { membershipRepository.find(membershipId) }
        confirmVerified(membershipRepository)
    }
}
