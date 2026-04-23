package io.miragon.example.domain

data class Age(val value: Int) {
    init {
        require(value >= 0) { "Age cannot be negative" }
    }
}
