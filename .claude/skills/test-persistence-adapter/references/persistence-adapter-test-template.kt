package io.miragon.example.adapter.outbound.db

import io.miragon.example.domain.SubscriptionId
import io.miragon.example.domain.testNewsletterSubscription
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import java.util.*

// @DataJpaTest loads only the JPA slice. The adapter is not in that slice — import it explicitly.
// @Autowired both the adapter under test and TestEntityManager (replaces any custom flush helper).
// Data strategy: only the save test writes programmatically; all others that need existing data use @Sql.
// SQL values must match the domain test builder so usingRecursiveComparison() passes without delta.
@DataJpaTest
@Import(NewsletterSubscriptionPersistenceAdapter::class)
class NewsletterSubscriptionPersistenceAdapterTest {

    @Autowired
    private lateinit var underTest: NewsletterSubscriptionPersistenceAdapter

    @Autowired
    private lateinit var entityManager: TestEntityManager

    private val id = SubscriptionId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))

    // Write programmatically; flush + clear to bypass the JPA first-level cache before re-reading.
    @Test
    fun `saves and reloads entity`() {
        val subscription = testNewsletterSubscription(id = id)

        underTest.save(subscription)
        entityManager.flush()
        entityManager.clear()

        assertThat(underTest.find(id)).usingRecursiveComparison().isEqualTo(subscription)
    }

    @Test
    fun `search returns null when not found`() {
        assertThat(underTest.search(id)).isNull()
    }

    @Test
    fun `find throws when not found`() {
        assertThatThrownBy { underTest.find(id) }
            .isInstanceOf(NoSuchElementException::class.java)
    }

    @Test
    @Sql("classpath:sql/newsletter-subscription.sql")
    fun `find returns entity when exists`() {
        assertThat(underTest.find(id))
            .usingRecursiveComparison()
            .isEqualTo(testNewsletterSubscription(id = id))
    }

    @Test
    @Sql("classpath:sql/newsletter-subscription.sql")
    fun `search returns entity when exists`() {
        assertThat(underTest.search(id))
            .usingRecursiveComparison()
            .isEqualTo(testNewsletterSubscription(id = id))
    }

    // flush + clear after delete to ensure the row is gone in the database, not just the cache.
    @Test
    @Sql("classpath:sql/newsletter-subscription.sql")
    fun `deletes existing entity`() {
        underTest.delete(id)
        entityManager.flush()
        entityManager.clear()

        assertThat(underTest.search(id)).isNull()
    }
}
