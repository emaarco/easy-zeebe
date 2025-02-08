package de.emaarco.example.adapter.inbound.rest

import de.emaarco.example.application.port.inbound.SubscribeToNewsletterUseCase
import de.emaarco.example.domain.Email
import de.emaarco.example.domain.Name
import de.emaarco.example.domain.NewsletterId
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

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
