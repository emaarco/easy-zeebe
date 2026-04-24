package io.miragon.example.adapter.inbound.zeebe

import io.miragon.example.adapter.process.MiraveloMembershipProcessApi.Variables.ServiceTaskClaimMembership
import io.miragon.example.application.port.inbound.ClaimMembershipUseCase
import io.miragon.example.domain.MembershipId
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*
import java.util.Map.entry

class ClaimMembershipWorkerTest {

    private val useCase = mockk<ClaimMembershipUseCase>()
    private val underTest = ClaimMembershipWorker(useCase)

    @Test
    fun `should return hasEmptySpots true when spot is available`() {

        // Given
        val membershipId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        every { useCase.claim(MembershipId(membershipId)) } returns true

        // When
        val result = underTest.handle(membershipId)

        // Then
        assertThat(result).containsExactly(entry(ServiceTaskClaimMembership.HAS_EMPTY_SPOTS.value, true))
        verify(exactly = 1) { useCase.claim(MembershipId(membershipId)) }
        confirmVerified(useCase)
    }

    @Test
    fun `should return hasEmptySpots false when capacity is exceeded`() {

        // Given
        val membershipId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001")
        every { useCase.claim(MembershipId(membershipId)) } returns false

        // When
        val result = underTest.handle(membershipId)

        // Then
        assertThat(result).containsExactly(entry(ServiceTaskClaimMembership.HAS_EMPTY_SPOTS.value, false))
        verify(exactly = 1) { useCase.claim(MembershipId(membershipId)) }
        confirmVerified(useCase)
    }
}
