package io.miragon.example.domain

import java.time.LocalDateTime
import java.util.*

data class Membership(
    val id: MembershipId = MembershipId(UUID.randomUUID()),
    val name: Name,
    val email: Email,
    val age: Age,
    val registrationDate: LocalDateTime = LocalDateTime.now(),
    val status: MembershipStatus = MembershipStatus.PENDING,
) {
    fun confirmMembership() = this.copy(status = MembershipStatus.CONFIRMED)
    fun rejectMembership() = this.copy(status = MembershipStatus.REJECTED)
    fun declineMembership() = this.copy(status = MembershipStatus.DECLINED)
}
