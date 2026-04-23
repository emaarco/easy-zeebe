package io.miragon.example.application.port.outbound

import io.miragon.example.domain.MembershipId

interface MembershipProcess {
    fun registerMembership(id: MembershipId): Long
    fun confirmMembership(id: MembershipId)
}
