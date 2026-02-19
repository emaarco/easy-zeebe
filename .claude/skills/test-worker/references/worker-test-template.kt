// Worker unit test template — replace all placeholders before use
// Placeholders: WorkerName, UseCaseInterface, useCaseMethod, DomainType, handle args

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.UUID

class WorkerNameTest {

    private val useCase = mockk<UseCaseInterface>()
    private val underTest = WorkerName(useCase)

    @Test
    fun `should perform action when job is received`() {

        // Given
        val subscriptionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        every { useCase.useCaseMethod(DomainType(subscriptionId)) } just Runs

        // When
        underTest.handle(subscriptionId)

        // Then
        verify(exactly = 1) { useCase.useCaseMethod(DomainType(subscriptionId)) }
        confirmVerified(useCase)
    }
}

// Variations:
// @VariableAsType: underTest.handle(MyVarsClass(subscriptionId = subscriptionId))
// One test per happy-path behavior; ask before adding more

// Returning Map variant (worker returns Map<String, Any> output variables):
// Additional imports needed:
//   import org.assertj.core.api.Assertions.assertThat
//   import java.util.Map.entry
//
// val result = underTest.handle(subscriptionId)
// assertThat(result).containsExactly(entry(ProcessApi.Variables.VARIABLE_NAME, expectedValue))