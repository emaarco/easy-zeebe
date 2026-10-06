// Worker class template — replace all placeholders before use
// Placeholders: WorkerName, ServiceTasks.CONSTANT, UseCaseInterface, useCaseMethod, DomainType, ProcessVariables.CONSTANT

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

    @JobWorker(type = ServiceTasks.CONSTANT)
    fun handle(@Variable(name = ProcessVariables.SUBSCRIPTION_ID) subscriptionId: UUID) {
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

    @JobWorker(type = ServiceTasks.CONSTANT)
    fun handle(@Variable(name = ProcessVariables.SUBSCRIPTION_ID) subscriptionId: UUID): Map<String, Any> {
        log.debug { "Received job for subscriptionId: $subscriptionId" }
        useCase.useCaseMethod(DomainType(subscriptionId))
        return mapOf(ProcessVariables.VARIABLE_NAME to value)
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

    @JobWorker(type = ServiceTasks.CONSTANT)
    fun handle(@Variable(name = ProcessVariables.SUBSCRIPTION_ID) subscriptionId: UUID): Map<String, Any> {
        log.debug { "Received job for subscriptionId: $subscriptionId" }
        val result = useCase.useCaseMethod(DomainType(subscriptionId))

        return when (result) {
            null -> mapOf(ProcessVariables.FOUND to false)
            else -> mapOf(
                ProcessVariables.FOUND to true,
                ProcessVariables.RESULT to result.asValue()
            )
        }
    }
}

// Variations:
// @VariableAsType: fun handle(@VariableAsType variables: MyVarsClass) { ... }
// Multiple static output variables: return mapOf(ProcessVariables.A to valueA, ProcessVariables.B to valueB)