package io.miragon.example.domain

import java.util.*

data class SubscriptionId(val value: UUID) {
    constructor(value: String) : this(UUID.fromString(value))
}
