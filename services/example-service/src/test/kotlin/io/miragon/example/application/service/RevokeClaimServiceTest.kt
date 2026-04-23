package io.miragon.example.application.service

import io.miragon.example.domain.MembershipId
import org.junit.jupiter.api.Test
import java.util.*

class RevokeClaimServiceTest {

    private val underTest = RevokeClaimService()

    @Test
    fun `revoke claim completes without error`() {

        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))

        underTest.revokeClaim(membershipId)
    }
}
