package io.miragon.example.domain

import java.time.LocalDateTime
import java.util.*

fun testMembership(
    id: MembershipId = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")),
    name: Name = Name("John Doe"),
    email: Email = Email("john.doe@test.com"),
    age: Age = Age(30),
    registrationDate: LocalDateTime = LocalDateTime.parse("2024-01-15T10:30:00"),
    status: MembershipStatus = MembershipStatus.PENDING,
) = Membership(
    id = id,
    name = name,
    email = email,
    age = age,
    registrationDate = registrationDate,
    status = status,
)
