// Worker class template — replace all placeholders before use
// Placeholders: WorkerName, TaskTypes.CONSTANT, UseCaseInterface, useCaseMethod, DomainType, Variables.CONSTANT

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.UUID

// ---------------------------------------------------------------------------
// Void variant (no output variables — handle returns Unit):
// ---------------------------------------------------------------------------

@Component
class <Name>Worker(
    private val useCase: UseCaseInterface
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.CONSTANT)
    fun handle(@Variable subscriptionId: UUID) {
        log.debug { "Received job for subscriptionId: $subscriptionId" }
        useCase.useCaseMethod(DomainType(subscriptionId))
    }
}

// ---------------------------------------------------------------------------
// Return variant (worker produces output variables — handle returns Map<String, Any>):
// ---------------------------------------------------------------------------

@Component
class <Name>Worker(
    private val useCase: UseCaseInterface
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.CONSTANT)
    fun handle(@Variable subscriptionId: UUID): Map<String, Any> {
        log.debug { "Received job for subscriptionId: $subscriptionId" }
        useCase.useCaseMethod(DomainType(subscriptionId))
        return mapOf(Variables.VARIABLE_NAME to value)
    }
}

// ---------------------------------------------------------------------------
// Dynamic output variant (map contents depend on the use-case result):
// ---------------------------------------------------------------------------

@Component
class <Name>Worker(
    private val useCase: UseCaseInterface
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.CONSTANT)
    fun handle(@Variable subscriptionId: UUID): Map<String, Any> {
        log.debug { "Received job for subscriptionId: $subscriptionId" }
        val result = useCase.useCaseMethod(DomainType(subscriptionId))

        return when (result) {
            null -> mapOf(Variables.FOUND to false)
            else -> mapOf(
                Variables.FOUND to true,
                Variables.RESULT to result.asValue()
            )
        }
    }
}

// Variations:
// @VariableAsType: fun handle(@VariableAsType variables: MyVarsClass) { ... }
// Multiple static output variables: return mapOf(Variables.A to valueA, Variables.B to valueB)