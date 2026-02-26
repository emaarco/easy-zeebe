# Full Pattern: JPA Persistence Adapter

Complete example for a `NewsletterSubscription` aggregate.

## NewsletterSubscriptionEntity.kt

```kotlin
@Entity(name = "newsletter_subscription")
data class NewsletterSubscriptionEntity(

    @Id
    @Column(name = "subscription_id", nullable = false)
    val subscriptionId: UUID,

    @Column(name = "subscriber_name", nullable = false)
    val name: String,

    @Column(name = "subscriber_mail", nullable = false)
    val email: String,

    @Column(name = "newsletter_id", nullable = false)
    val newsletterId: UUID,

    @Column(name = "registration_date", nullable = false)
    val registrationDate: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false)
    val status: SubscriptionStatus = SubscriptionStatus.PENDING
)
```

## NewsletterSubscriptionJpaRepository.kt

```kotlin
interface NewsletterSubscriptionJpaRepository : JpaRepository<NewsletterSubscriptionEntity, UUID> {
    fun findBySubscriptionId(id: UUID): NewsletterSubscriptionEntity?
}
```

## NewsletterSubscriptionEntityMapper.kt

```kotlin
object NewsletterSubscriptionEntityMapper {

    fun toDomain(
        entity: NewsletterSubscriptionEntity
    ) = NewsletterSubscription(
        id = SubscriptionId(entity.subscriptionId),
        name = Name(entity.name),
        email = Email(entity.email),
        newsletter = NewsletterId(entity.newsletterId),
        registrationDate = entity.registrationDate,
        status = entity.status
    )

    fun toEntity(
        domain: NewsletterSubscription
    ) = NewsletterSubscriptionEntity(
        subscriptionId = domain.id.value,
        name = domain.name.value,
        email = domain.email.value,
        newsletterId = domain.newsletter.value,
        registrationDate = domain.registrationDate,
        status = domain.status
    )
}
```

## NewsletterSubscriptionPersistenceAdapter.kt

```kotlin
@Component
class NewsletterSubscriptionPersistenceAdapter(
    private val repository: NewsletterSubscriptionJpaRepository
) : NewsletterSubscriptionRepository {

    override fun find(subscriptionId: SubscriptionId): NewsletterSubscription {
        val entity = repository.findBySubscriptionId(subscriptionId.value)
            ?: throw NoSuchElementException()
        return NewsletterSubscriptionEntityMapper.toDomain(entity)
    }

    override fun search(subscriptionId: SubscriptionId): NewsletterSubscription? {
        val entity = repository.findBySubscriptionId(subscriptionId.value) ?: return null
        return NewsletterSubscriptionEntityMapper.toDomain(entity)
    }

    override fun save(subscription: NewsletterSubscription) {
        val entity = NewsletterSubscriptionEntityMapper.toEntity(subscription)
        repository.save(entity)
    }

    override fun delete(subscriptionId: SubscriptionId) {
        repository.deleteById(subscriptionId.value)
    }
}
```