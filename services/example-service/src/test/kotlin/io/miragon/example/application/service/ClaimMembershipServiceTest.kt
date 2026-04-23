package io.miragon.example.application.service

import io.miragon.example.domain.MembershipId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class ClaimMembershipServiceTest {

    private val underTest = ClaimMembershipService()

    @Test
    fun `returns true when capacity is available`() {
        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val result = underTest.claim(membershipId)
        assertThat(result).isTrue()
    }
}
