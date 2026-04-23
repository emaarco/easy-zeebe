package io.miragon.example.adapter.inbound.rest

import io.miragon.example.application.port.inbound.RejectConfirmationUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/memberships")
class RejectConfirmationController(private val useCase: RejectConfirmationUseCase) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/reject-confirmation/{membershipId}")
    fun rejectConfirmation(@PathVariable membershipId: String): ResponseEntity<Void> {
        log.debug { "Received REST-request to reject confirmation for membership: $membershipId" }
        useCase.rejectConfirmation(MembershipId(UUID.fromString(membershipId)))
        return ResponseEntity.ok().build()
    }
}
