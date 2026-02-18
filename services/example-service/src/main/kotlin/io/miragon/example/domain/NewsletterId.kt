package io.miragon.example.domain

import java.util.*

data class NewsletterId(val value: UUID) {
    constructor(value: String) : this(UUID.fromString(value))
}