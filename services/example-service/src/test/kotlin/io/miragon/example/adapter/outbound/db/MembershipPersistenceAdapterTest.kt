package io.miragon.example.adapter.outbound.db

import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.testMembership
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import java.util.*

@DataJpaTest
@Import(MembershipPersistenceAdapter::class)
class MembershipPersistenceAdapterTest {

    @Autowired
    private lateinit var underTest: MembershipPersistenceAdapter

    @Autowired
    private lateinit var entityManager: TestEntityManager

    private val id = MembershipId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))

    @Test
    fun `saves and reloads entity`() {
        val membership = testMembership(id = id)

        underTest.save(membership)
        entityManager.flush()
        entityManager.clear()

        assertThat(underTest.find(id)).usingRecursiveComparison().isEqualTo(membership)
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
    @Sql("classpath:sql/membership.sql")
    fun `find returns entity when exists`() {
        assertThat(underTest.find(id))
            .usingRecursiveComparison()
            .isEqualTo(testMembership(id = id))
    }

    @Test
    @Sql("classpath:sql/membership.sql")
    fun `search returns entity when exists`() {
        assertThat(underTest.search(id))
            .usingRecursiveComparison()
            .isEqualTo(testMembership(id = id))
    }

    @Test
    @Sql("classpath:sql/membership.sql")
    fun `deletes existing entity`() {
        underTest.delete(id)
        entityManager.flush()
        entityManager.clear()

        assertThat(underTest.search(id)).isNull()
    }
}
