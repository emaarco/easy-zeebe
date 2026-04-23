package io.miragon.example.application.port.outbound

import io.miragon.example.domain.MembershipId

/**
 * Outbound port for interactions with the `miravelo-membership` Zeebe process.
 *
 * Implementations may use different engine mechanisms per method:
 * - [registerMembership] and [rejectConfirmation] publish messages (fire-and-forget).
 * - [confirmMembership] completes the currently active `userTask_ConfirmMembership` —
 *   it looks up the task for the given [MembershipId] and therefore may fail if no
 *   active confirmation task exists for the member.
 */
interface MembershipProcess {
    fun registerMembership(id: MembershipId)
    fun confirmMembership(id: MembershipId)
    fun rejectConfirmation(id: MembershipId)
}
