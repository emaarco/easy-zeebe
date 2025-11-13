package de.emaarco.example.domain

import java.time.LocalDateTime
import java.util.*

fun testNewsletterSubscription(
    id: SubscriptionId = SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")),
    name: Name = Name("John Doe"),
    email: Email = Email("john.doe@test.com"),
    newsletter: NewsletterId = NewsletterId(UUID.fromString("f51d9793-1b24-45db-bd6f-dd4cb26795e6")),
    registrationDate: LocalDateTime = LocalDateTime.parse("2024-01-15T10:30:00"),
    status: SubscriptionStatus = SubscriptionStatus.PENDING,
) = NewsletterSubscription(
    id = id,
    name = name,
    email = email,
    newsletter = newsletter,
    registrationDate = registrationDate,
    status = status,
)
