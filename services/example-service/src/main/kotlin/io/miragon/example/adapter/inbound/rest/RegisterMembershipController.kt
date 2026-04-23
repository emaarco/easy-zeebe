package io.miragon.example.adapter.inbound.rest

import io.miragon.example.application.port.inbound.RegisterMembershipUseCase
import io.miragon.example.domain.Age
import io.miragon.example.domain.Email
import io.miragon.example.domain.Name
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/memberships")
class RegisterMembershipController(private val useCase: RegisterMembershipUseCase) {

    private val log = KotlinLogging.logger {}

    @PostMapping
    fun registerMembership(@RequestBody input: MembershipForm): ResponseEntity<Response> {
        log.debug { "Received REST-request to register for MiraVelo membership: $input" }
        val membershipId = useCase.register(input.toCommand())
        return ResponseEntity.ok().body(Response(membershipId.value.toString()))
    }

    data class MembershipForm(
        val email: String,
        val name: String,
        val age: Int,
    )

    data class Response(val membershipId: String)

    private fun MembershipForm.toCommand() = RegisterMembershipUseCase.Command(
        Email(email),
        Name(name),
        Age(age),
    )
}
