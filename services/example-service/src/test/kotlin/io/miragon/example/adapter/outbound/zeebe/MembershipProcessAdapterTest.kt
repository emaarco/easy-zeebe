package io.miragon.example.adapter.outbound.zeebe

import io.miragon.common.zeebe.engine.ProcessEngineApi
import io.miragon.example.adapter.process.MiraveloMembershipProcessApi
import io.miragon.example.domain.MembershipId
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class MembershipProcessAdapterTest {

    private val engineApi = mockk<ProcessEngineApi>()
    private val underTest = MembershipProcessAdapter(engineApi = engineApi)

    @Test
    fun `starts miravelo membership process`() {

        // Given
        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        val expectedProcessInstanceKey = 42L
        val expectedVariables = mapOf(MiraveloMembershipProcessApi.Variables.MEMBERSHIP_ID to membershipId.value.toString())
        every { engineApi.startProcess(any(), any()) } returns expectedProcessInstanceKey

        // When
        val result = underTest.registerMembership(membershipId)

        // Then
        assertThat(result).isEqualTo(expectedProcessInstanceKey)
        verify {
            engineApi.startProcess(
                processId = MiraveloMembershipProcessApi.PROCESS_ID,
                variables = expectedVariables
            )
        }
        confirmVerified(engineApi)
    }

    @Test
    fun `confirmMembership sends message with correct parameters`() {

        // Given
        val membershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        every { engineApi.sendMessage(any(), any(), any()) } just Runs

        // When
        underTest.confirmMembership(membershipId)

        // Then
        verify {
            engineApi.sendMessage(
                messageName = MiraveloMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED,
                correlationId = membershipId.value.toString(),
                variables = emptyMap()
            )
        }
        confirmVerified(engineApi)
    }
}
