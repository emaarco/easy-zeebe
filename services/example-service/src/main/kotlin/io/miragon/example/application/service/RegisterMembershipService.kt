package io.miragon.example.application.service

import io.miragon.example.application.port.inbound.RegisterMembershipUseCase
import io.miragon.example.application.port.outbound.MembershipProcess
import io.miragon.example.application.port.outbound.MembershipRepository
import io.miragon.example.domain.Membership
import io.miragon.example.domain.MembershipId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class RegisterMembershipService(
    private val repository: MembershipRepository,
    private val processPort: MembershipProcess
) : RegisterMembershipUseCase {

    private val log = KotlinLogging.logger {}

    override fun register(command: RegisterMembershipUseCase.Command): MembershipId {
        val membership = buildMembership(command)
        repository.save(membership)
        processPort.registerMembership(membership.id)
        log.info { "Registered ${command.email} for MiraVelo membership" }
        return membership.id
    }

    private fun buildMembership(command: RegisterMembershipUseCase.Command) = Membership(
        email = command.email,
        name = command.name,
        age = command.age,
    )
}
