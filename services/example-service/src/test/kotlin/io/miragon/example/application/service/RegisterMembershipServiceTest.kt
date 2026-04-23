package io.miragon.example.application.service

import io.miragon.example.application.port.inbound.RegisterMembershipUseCase
import io.miragon.example.application.port.outbound.MembershipProcess
import io.miragon.example.application.port.outbound.MembershipRepository
import io.miragon.example.domain.Age
import io.miragon.example.domain.Email
import io.miragon.example.domain.Membership
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.Name
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RegisterMembershipServiceTest {

    private val processPort = mockk<MembershipProcess>()
    private val membershipRepository = mockk<MembershipRepository>()
    private val underTest = RegisterMembershipService(
        repository = membershipRepository,
        processPort = processPort
    )

    @Test
    fun `register membership`() {

        val captor = slot<Membership>()
        every { membershipRepository.save(capture(captor)) } just Runs
        every { processPort.registerMembership(any<MembershipId>()) } just Runs

        val command = RegisterMembershipUseCase.Command(
            name = Name("John Doe"),
            email = Email("john.doe@test.com"),
            age = Age(30),
        )

        val membershipId = underTest.register(command)

        assertThat(membershipId).isNotNull
        verify { processPort.registerMembership(membershipId) }
        verify { membershipRepository.save(captor.captured) }
        confirmVerified(processPort, membershipRepository)
    }

}
