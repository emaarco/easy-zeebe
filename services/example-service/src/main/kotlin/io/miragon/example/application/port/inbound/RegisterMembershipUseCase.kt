package io.miragon.example.application.port.inbound

import io.miragon.example.domain.Age
import io.miragon.example.domain.Email
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.Name

interface RegisterMembershipUseCase {

    fun register(command: Command): MembershipId

    data class Command(
        val email: Email,
        val name: Name,
        val age: Age,
    )
}
