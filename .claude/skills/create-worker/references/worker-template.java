// Worker class template (Java) — replace all placeholders before use
// Placeholders: <Name>Worker, TaskTypes.CONSTANT, UseCaseInterface, useCaseMethod, DomainType, Variables.CONSTANT

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

// ---------------------------------------------------------------------------
// Void variant (no output variables — handle returns void):
// ---------------------------------------------------------------------------

@Component
public class <Name>Worker {

    private static final Logger log = LoggerFactory.getLogger(<Name>Worker.class);

    private final UseCaseInterface useCase;

    public <Name>Worker(UseCaseInterface useCase) {
        this.useCase = useCase;
    }

    @JobWorker(type = TaskTypes.CONSTANT)
    public void handle(@Variable UUID subscriptionId) {
        log.debug("Received job for subscriptionId: {}", subscriptionId);
        useCase.useCaseMethod(new DomainType(subscriptionId));
    }
}

// ---------------------------------------------------------------------------
// Return variant (worker produces output variables — handle returns Map<String, Object>):
// ---------------------------------------------------------------------------

@Component
public class <Name>Worker {

    private static final Logger log = LoggerFactory.getLogger(<Name>Worker.class);

    private final UseCaseInterface useCase;

    public <Name>Worker(UseCaseInterface useCase) {
        this.useCase = useCase;
    }

    @JobWorker(type = TaskTypes.CONSTANT)
    public Map<String, Object> handle(@Variable UUID subscriptionId) {
        log.debug("Received job for subscriptionId: {}", subscriptionId);
        useCase.useCaseMethod(new DomainType(subscriptionId));
        return Map.of(Variables.VARIABLE_NAME, value);
    }
}

// Variations:
// @VariableAsType: public void handle(@VariableAsType MyVarsClass variables) { ... }
// Multiple output variables: return Map.of(Variables.A, valueA, Variables.B, valueB);
// Lombok: replace logger field + constructor with @Slf4j and @RequiredArgsConstructor on the class