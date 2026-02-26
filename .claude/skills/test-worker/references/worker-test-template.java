// Worker unit test template (Java) — replace all placeholders before use
// Placeholders: WorkerName, UseCaseInterface, useCaseMethod, DomainType, handle args

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class WorkerNameTest {

    @Mock
    UseCaseInterface useCase;

    @InjectMocks
    WorkerName underTest;

    @Test
    void shouldPerformActionWhenJobIsReceived() {

        // Given
        UUID subscriptionId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        doNothing().when(useCase).useCaseMethod(new DomainType(subscriptionId));

        // When
        underTest.handle(subscriptionId);

        // Then
        verify(useCase).useCaseMethod(new DomainType(subscriptionId));
        verifyNoMoreInteractions(useCase);
    }
}

// Variations:
// @VariableAsType: underTest.handle(new MyVarsClass(subscriptionId));
// One test per happy-path behavior; ask before adding more

// Returning Map variant (worker returns Map<String, Object> output variables):
// Additional imports needed:
//   import org.assertj.core.api.Assertions.assertThat;
//   import java.util.Map;
//
// Map<String, Object> result = underTest.handle(subscriptionId);
// assertThat(result).containsExactly(Map.entry(ProcessApi.Variables.VARIABLE_NAME, expectedValue));