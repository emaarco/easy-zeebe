package io.miragon.example.adapter.outbound.db

import io.miragon.example.domain.MembershipStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.util.*

@Entity(name = "membership")
data class MembershipEntity(

    @Id
    @Column(name = "membership_id", nullable = false)
    val membershipId: UUID,

    @Column(name = "member_name", nullable = false)
    val name: String,

    @Column(name = "member_email", nullable = false)
    val email: String,

    @Column(name = "age", nullable = false)
    val age: Int,

    @Column(name = "registration_date", nullable = false)
    val registrationDate: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status", nullable = false)
    val status: MembershipStatus = MembershipStatus.PENDING

)
