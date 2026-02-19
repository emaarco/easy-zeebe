package io.miragon.example.adapter.inbound.rest

import io.miragon.example.application.port.inbound.SubscribeToNewsletterUseCase
import io.miragon.example.domain.Email
import io.miragon.example.domain.Name
import io.miragon.example.domain.NewsletterId
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

// --- POST with request body ---
// One controller per use case; inject the use-case interface as the only constructor parameter.
// Nested data class DTOs; toCommand() wraps raw strings in domain value objects — never pass raw strings to the use case.
@RestController
@RequestMapping("/api/subscriptions")
class SubscribeToNewsletterController(private val useCase: SubscribeToNewsletterUseCase) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/subscribe")
    fun subscribeToNewsletter(@RequestBody input: SubscriptionForm): ResponseEntity<Response> {
        log.debug { "Received REST-request to subscribe to newsletter: $input" }
        val subscriptionId = useCase.subscribe(input.toCommand())
        return ResponseEntity.ok().body(Response(subscriptionId.value.toString()))
    }

    data class SubscriptionForm(
        val email: String,
        val name: String,
        val newsletterId: String
    )

    data class Response(val subscriptionId: String)

    private fun SubscriptionForm.toCommand() = SubscribeToNewsletterUseCase.Command(
        Email(email),
        Name(name),
        NewsletterId(UUID.fromString(newsletterId))
    )
}

// --- POST/DELETE with path variable (void use case, no response body) ---
// Use ResponseEntity<Void> and return ResponseEntity.ok().build().
// @PathVariable receives a raw String; wrap it in the domain type before calling the use case.
//
// @RestController
// @RequestMapping("/api/subscriptions")
// class ConfirmSubscriptionController(private val useCase: ConfirmSubscriptionUseCase) {
//
//     private val log = KotlinLogging.logger {}
//
//     @PostMapping("/confirm/{subscriptionId}")
//     fun confirmSubscription(@PathVariable subscriptionId: String): ResponseEntity<Void> {
//         log.debug { "Received REST-request to confirm subscription: $subscriptionId" }
//         useCase.confirm(SubscriptionId(UUID.fromString(subscriptionId)))
//         return ResponseEntity.ok().build()
//     }
// }
