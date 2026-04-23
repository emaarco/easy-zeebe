package io.miragon.example.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MembershipTest {

    @Test
    fun `confirm membership changes status to CONFIRMED`() {
        val membership = testMembership(status = MembershipStatus.PENDING)
        val confirmed = membership.confirmMembership()
        assertThat(confirmed).isEqualTo(membership.copy(status = MembershipStatus.CONFIRMED))
    }

    @Test
    fun `reject membership changes status to REJECTED`() {
        val membership = testMembership(status = MembershipStatus.PENDING)
        val rejected = membership.rejectMembership()
        assertThat(rejected).isEqualTo(membership.copy(status = MembershipStatus.REJECTED))
    }
}
