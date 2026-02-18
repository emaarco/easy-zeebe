---
name: test-persistence-adapter
description: Generate or fix a test for a persistence adapter using @DataJpaTest with H2 in-memory database. Use when the user asks to write, generate, or fix tests for a persistence adapter.
argument-hint: "<path-to-adapter-file-or-test-file>"
allowed-tools: Read, Write, Edit, Glob, Bash(./gradlew *)
---

# Skill: test-persistence-adapter

Generate or fix a test for a persistence adapter using `@DataJpaTest` with H2 in-memory database.

## Usage

```
/test-persistence-adapter <path-to-adapter-file>
```

Example:

```
/test-persistence-adapter services/example-service/src/main/kotlin/io/miragon/example/adapter/outbound/db/NewsletterSubscriptionPersistenceAdapter.kt
```

## Pattern

`@DataJpaTest` loads only the JPA slice (no full Spring context).
The adapter itself is not part of that slice and must be imported explicitly.
`TestEntityManager` replaces any custom cache-flush helper.

```kotlin
@DataJpaTest
@Import(SomePersistenceAdapter::class)
class SomePersistenceAdapterTest {

    @Autowired
    private lateinit var underTest: SomePersistenceAdapter

    @Autowired
    private lateinit var entityManager: TestEntityManager

    private val id = SomeId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))

    @Test
    fun `saves and reloads entity`() {
        val aggregate = testAggregate(id = id)

        underTest.save(aggregate)
        entityManager.flush()
        entityManager.clear()

        assertThat(underTest.find(id)).usingRecursiveComparison().isEqualTo(aggregate)
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
    @Sql("classpath:sql/test-data.sql")
    fun `finds existing entity`() {
        assertThat(underTest.find(id)).usingRecursiveComparison().isEqualTo(testAggregate(id = id))
    }

    @Test
    @Sql("classpath:sql/test-data.sql")
    fun `search returns existing entity`() {
        assertThat(underTest.search(id)).usingRecursiveComparison().isEqualTo(testAggregate(id = id))
    }

    @Test
    @Sql("classpath:sql/test-data.sql")
    fun `deletes existing entity`() {
        underTest.delete(id)
        entityManager.flush()
        entityManager.clear()

        assertThat(underTest.search(id)).isNull()
    }
}
```

## Key Rules

- `@DataJpaTest` + `@Import(AdapterClass::class)` — no full Spring context, no custom base class
- `@Autowired` inject both the adapter under test and `TestEntityManager`
- Call `entityManager.flush()` then `entityManager.clear()` after every write before re-reading,
  to ensure the JPA first-level cache does not mask a missing database round-trip
- **Data strategy**: only the `save` test writes data programmatically; every other test that needs
  existing data uses `@Sql` — this keeps tests independent of each other
- Use `@Sql("classpath:sql/<entity>.sql")` for `find`, `search` (found path), and `delete` tests; SQL
  files go in `src/test/resources/sql/` — derive column names from the entity's `@Column` annotations
  and values from the domain test builder so `usingRecursiveComparison()` assertions pass without delta
- Use `assertThat(result).usingRecursiveComparison().isEqualTo(expected)` for aggregate comparisons
- Use test builders from `domain/TestObjectBuilder.kt` for test data
- Fixed UUID strings for deterministic test data

## Instructions

1. **Read and identify** — read the adapter file at `$ARGUMENTS` and extract:
    - Adapter class name and the outbound port interface it implements
    - Public methods, their signatures, and whether they read or write
    - JPA repository used, entity class, domain/value types, ID type

   Then read the entity class to understand `@Column` mappings (needed for SQL fixtures).

2. **Locate the test file** — mirror `src/main/kotlin` → `src/test/kotlin`, same package path, append `Test` to the
   class name. If the file already exists, open it and switch to fix mode. If not, proceed to generate a new file.

3. **Determine required test cases** — standard coverage for a persistence adapter (up to 6 tests):
    - save+reload (programmatic write), find (SQL), search-found (SQL), search-null (no data),
      find-throws (no data), delete (SQL)

   Only include tests for methods that actually exist on the adapter. If more scenarios apply, list them and ask for
   permission first.

4. **Generate or fix the test file**:
    - `@DataJpaTest` + `@Import(AdapterClass::class)`; `@Autowired` the adapter and `TestEntityManager`
    - Only the save test writes data programmatically; all other tests that need existing data use `@Sql`
    - `entityManager.flush()` then `entityManager.clear()` after every write before re-reading
    - SQL fixtures in `src/test/resources/sql/` — derive column names from `@Column` annotations; values
      must match the domain test builder so `usingRecursiveComparison()` passes without delta
    - Use test builders from `domain/TestObjectBuilder.kt` for domain objects
    - Fixed UUID strings for deterministic test data

5. **Write, run, and report** — write the test file and any required SQL fixture; run all tests in the
   class via an appropriate Gradle command; report the created or updated file path and a brief summary.
