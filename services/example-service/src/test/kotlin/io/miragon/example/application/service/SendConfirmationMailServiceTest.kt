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

class SendConfirmationMailServiceTest {

    private val membershipRepository = mockk<MembershipRepository>()
    private val underTest = SendConfirmationMailService(repository = membershipRepository)

    @Test
    fun `send confirmation mail`() {

        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val membership = testMembership(id = membershipId)

        every { membershipRepository.find(membershipId) } returns membership

        underTest.sendConfirmationMail(membershipId)

        verify { membershipRepository.find(membershipId) }
        confirmVerified(membershipRepository)
    }
}
