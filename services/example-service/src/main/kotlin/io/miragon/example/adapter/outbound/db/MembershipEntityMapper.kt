package io.miragon.example.adapter.outbound.db

import io.miragon.example.domain.Age
import io.miragon.example.domain.Email
import io.miragon.example.domain.Membership
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.Name

object MembershipEntityMapper {

    fun toDomain(entity: MembershipEntity): Membership {
        return Membership(
            id = MembershipId(entity.membershipId),
            name = Name(entity.name),
            email = Email(entity.email),
            age = Age(entity.age),
            registrationDate = entity.registrationDate,
            status = entity.status
        )
    }

    fun toEntity(domain: Membership): MembershipEntity {
        return MembershipEntity(
            membershipId = domain.id.value,
            name = domain.name.value,
            email = domain.email.value,
            age = domain.age.value,
            registrationDate = domain.registrationDate,
            status = domain.status
        )
    }

}
