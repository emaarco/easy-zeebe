package de.emaarco.example.adapter.outbound.db

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface NewsletterSubscriptionJpaRepository : JpaRepository<NewsletterSubscriptionEntity, UUID> {
    fun findBySubscriptionId(id: UUID): NewsletterSubscriptionEntity?
}