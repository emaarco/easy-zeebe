---
name: create-persistence-adapter
description: Scaffold or update a JPA persistence adapter (outbound adapter). Accepts a repository port file, a domain entity, or a plain description. Use when the user wants to generate the JPA persistence layer for a domain aggregate.
argument-hint: "[<path-to-Repository-port-file-or-domain-entity>]"
allowed-tools: Read, Write, Glob
---

# Skill: create-persistence-adapter

Generate or update a JPA persistence adapter (outbound adapter) for a domain aggregate.

## What is a Persistence Adapter?

A persistence adapter is an **outbound adapter** that acts as the interaction layer between your application and the
database — your code calls it via a port interface, and it handles the translation between domain objects and the
underlying storage mechanism (e.g. JPA/Spring Data).

## Usage

```
/create-persistence-adapter [<path-to-Repository-port-file-or-domain-entity>]
```

Examples:

```
# From a repository port file
/create-persistence-adapter services/example-service/src/main/kotlin/io/miragon/example/application/port/outbound/NewsletterSubscriptionRepository.kt

# From a domain entity file — skill finds the repository port automatically
/create-persistence-adapter services/example-service/src/main/kotlin/io/miragon/example/domain/NewsletterSubscription.kt

# No arguments — skill searches for repository ports and asks which one to use
/create-persistence-adapter
```

## What This Skill Creates or Updates

Up to four files per aggregate, all in `adapter/outbound/db/`:

- `*Entity.kt` — JPA entity mapping the aggregate to a database table
- `*JpaRepository.kt` — Spring Data `JpaRepository` interface
- `*EntityMapper.kt` — singleton `object` for entity ↔ domain conversion
- `*PersistenceAdapter.kt` — `@Component` implementing the outbound repository port

If any of these files already exist, the skill reads them,
compares them against the port interface and domain entity,
and applies only what is missing or outdated.

## Pattern

```kotlin
// NewsletterSubscriptionEntity.kt
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

// NewsletterSubscriptionJpaRepository.kt
interface NewsletterSubscriptionJpaRepository : JpaRepository<NewsletterSubscriptionEntity, UUID> {
    fun findBySubscriptionId(id: UUID): NewsletterSubscriptionEntity?
}

// NewsletterSubscriptionEntityMapper.kt
object NewsletterSubscriptionEntityMapper {

    fun toDomain(entity: NewsletterSubscriptionEntity): NewsletterSubscription =
        NewsletterSubscription(
            id = SubscriptionId(entity.subscriptionId),
            name = Name(entity.name),
            email = Email(entity.email),
            newsletter = NewsletterId(entity.newsletterId),
            registrationDate = entity.registrationDate,
            status = entity.status
        )

    fun toEntity(domain: NewsletterSubscription): NewsletterSubscriptionEntity =
        NewsletterSubscriptionEntity(
            subscriptionId = domain.id.value,
            name = domain.name.value,
            email = domain.email.value,
            newsletterId = domain.newsletter.value,
            registrationDate = domain.registrationDate,
            status = domain.status
        )
}

// NewsletterSubscriptionPersistenceAdapter.kt
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

## Key Rules

- Implement **every method** defined in the outbound repository port interface
- `find` throws `NoSuchElementException` when the entity is not found; `search` returns `null`
- All entity ↔ domain mapping goes through the singleton mapper `object` — never inline conversion
  in the adapter or the JPA repository
- The mapper is a Kotlin `object` (singleton), not a `class` or a `companion object`
- Column names use `snake_case`; the `@Entity(name = ...)` value matches the database table name
- Enum fields use `@Enumerated(EnumType.STRING)` — never store the ordinal
- The JPA repository extends `JpaRepository<EntityClass, UUID>` and adds one `findBy<IdField>`
  method returning a nullable entity
- Before writing the entity, read the domain aggregate class to discover all fields and their types.
  Map each domain value object to its primitive (`String`, `UUID`, `LocalDateTime`, etc.) in the entity.
- Prefer constructor injection for the JPA repository in the adapter; never use field injection

## Instructions

### Step 1 – Resolve the input source

Determine which aggregate to target based on `$ARGUMENTS`:

- **Repository port file (`.kt`)** in `port/outbound/`: read it directly. Extract: interface name,
  all method signatures (names, parameter types, return types).
  Then search for the domain aggregate class (Glob `**/domain/<AggregateName>.kt`) and read it to
  discover fields.
- **Domain entity file (`.kt`)** in `domain/`: read it directly. Then search for the matching
  repository port (Glob `**/port/outbound/*Repository.kt`) and read it if found.
- **No argument / plain description**: search the whole codebase for repository ports
  (Glob `**/application/port/outbound/*Repository.kt`). List them and ask which one to use.

### Step 2 – Determine the target package and source root

Derive the base package from the repository port package
(e.g. `io.miragon.example.application.port.outbound` → base: `io.miragon.example`).
Adapter package: `<base>.adapter.outbound.db`.

Determine the source root path from the port file path
(e.g. `services/example-service/src/main/kotlin/`).

### Step 3 – Determine the aggregate fields and table mapping

Read the domain aggregate class. For each field:

- Map domain value objects to their primitive type in the entity
  (e.g. `Email(val value: String)` → `String`, `SubscriptionId(val value: UUID)` → `UUID`)
- Derive the column name as `snake_case` of the field name
  (e.g. `registrationDate` → `registration_date`, `newsletterId` → `newsletter_id`)
- Mark enum fields with `@Enumerated(EnumType.STRING)`
- Mark the ID field with `@Id` and `@Column(name = "<id-column>", nullable = false)`

Derive the table name as `snake_case` of the aggregate class name
(e.g. `NewsletterSubscription` → `newsletter_subscription`).

### Step 4 – Generate or update all four files

Derive class names from the aggregate name (e.g. `NewsletterSubscription`):

- Entity: `NewsletterSubscriptionEntity`
- JPA Repository: `NewsletterSubscriptionJpaRepository`
- Mapper: `NewsletterSubscriptionEntityMapper`
- Adapter: `NewsletterSubscriptionPersistenceAdapter`

Check which files already exist at the target location.

**For files that do not exist — generate:**

Follow the Pattern above for each file. Concretely:

- **Entity**: `data class` with `@Entity(name = "table_name")`. One field per aggregate property,
  mapped to its primitive type. `@Id` on the primary key field. `@Column(nullable = false)` on all
  fields. `@Enumerated(EnumType.STRING)` on enum fields. Use default values where the domain uses them.

- **JPA Repository**: `interface` extending `JpaRepository<EntityClass, UUID>`. Add a single
  `findBy<IdField>(id: UUID): EntityClass?` method for looking up by the aggregate ID.

- **Mapper**: `object` with two functions: `toDomain(entity: EntityClass): DomainClass` and
  `toEntity(domain: DomainClass): EntityClass`. Wrap each primitive in its domain value object in
  `toDomain`; extract `.value` in `toEntity`.

- **Adapter**: `@Component` class implementing the repository port. One constructor parameter:
  the JPA repository interface. Implement all port methods using the mapper for conversion.
  `find` throws `NoSuchElementException` on missing; `search` returns `null`; `save` delegates to
  `repository.save(...)`; `delete` delegates to `repository.deleteById(id.value)`.

**For files that already exist — update:**

Read the existing file. Compare it against the port interface and domain aggregate. Add any missing
methods or fields. Correct any outdated mappings. Preserve existing business logic.

### Step 5 – Report

List each file created, updated, or skipped (if nothing changed) and remind the developer to:

1. Add a database migration (Flyway/Liquibase) if a new table is introduced
2. Run `/test-persistence-adapter` to generate unit tests for the adapter
3. Wire the adapter into the application service via the port interface (automatic via Spring if
   the package is within the base component-scan path)