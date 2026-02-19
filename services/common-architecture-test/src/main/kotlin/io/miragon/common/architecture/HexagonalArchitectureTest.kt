package io.miragon.common.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import org.junit.jupiter.api.Test

/**
 * Abstract base class for hexagonal architecture tests.
 *
 * Enforces the ports-and-adapters (hexagonal) conventions for a Spring Boot service:
 *
 * ### Layer dependency rules
 * | Layer                 | May depend on                        |
 * |-----------------------|--------------------------------------|
 * | `domain`              | nothing                              |
 * | `application.port`    | `domain`                             |
 * | `application.service` | `domain`, `application.port`         |
 * | `adapter`             | `domain`, `application.port`         |
 *
 * Note: adapters access application services **only through port interfaces**,
 * never by importing `application.service` classes directly.
 *
 * ### Naming conventions
 * - Inbound ports (interfaces) → name ends with `UseCase` or `Query`
 * - Application services (classes) → name ends with `Service`
 *
 * ### Structural rules
 * - All types in `application.port.inbound` and `application.port.outbound` must be interfaces
 * - Each application service implements exactly one inbound port
 * - Application services do not depend on other application services
 * - Inbound adapters depend on at most one inbound port
 *
 * Concrete implementations must supply [rootPackage] to scope all checks.
 *
 * **Usage:**
 * ```kotlin
 * class MyHexagonalArchitectureTest : HexagonalArchitectureTest() {
 *     override val rootPackage = "com.example.myservice"
 * }
 * ```
 *
 * For an alternative ArchUnit-based implementation see
 * [spring-guardium-leviosa](https://github.com/emaarco/spring-guardium-leviosa).
 */
abstract class HexagonalArchitectureTest {

    /** The root package of the service under test, e.g. `"io.miragon.example"`. */
    abstract val rootPackage: String

    // -------------------------------------------------------------------------
    // Layer dependency rules
    // -------------------------------------------------------------------------

    /**
     * Enforces that architecture layers follow strict unidirectional dependencies.
     * This implicitly prevents circular dependencies between layers.
     */
    @Test
    fun `architecture layers have correct dependencies`() {
        Konsist.scopeFromProject()
            .assertArchitecture {
                val domain = Layer("Domain", "$rootPackage.domain..")
                val applicationPort = Layer("ApplicationPort", "$rootPackage.application.port..")
                val applicationService = Layer("ApplicationService", "$rootPackage.application.service..")
                val adapter = Layer("Adapter", "$rootPackage.adapter..")

                domain.dependsOnNothing()
                applicationPort.dependsOn(domain)
                applicationService.dependsOn(domain, applicationPort)
                adapter.dependsOn(domain, applicationPort)
            }
    }

    // -------------------------------------------------------------------------
    // Port interface rules
    // -------------------------------------------------------------------------

    /**
     * Inbound ports define the service API and must be interfaces so that
     * application services can implement them and adapters can depend on
     * abstractions rather than implementations.
     */
    @Test
    fun `inbound ports are interfaces`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.resideInPackage("$rootPackage.application.port.inbound..") }
            .assertTrue(additionalMessage = "All types in 'application.port.inbound' must be interfaces, not classes") {
                false
            }
    }

    /**
     * Outbound ports define the infrastructure API and must be interfaces so that
     * adapters (e.g. JPA, Zeebe) can provide interchangeable implementations.
     */
    @Test
    fun `outbound ports are interfaces`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.resideInPackage("$rootPackage.application.port.outbound..") }
            .assertTrue(additionalMessage = "All types in 'application.port.outbound' must be interfaces, not classes") {
                false
            }
    }

    // -------------------------------------------------------------------------
    // Naming conventions
    // -------------------------------------------------------------------------

    /**
     * Inbound ports represent either commands (`UseCase`) or queries (`Query`).
     * The suffix makes the intent immediately clear from the type name.
     */
    @Test
    fun `inbound ports end with UseCase or Query`() {
        Konsist.scopeFromProject()
            .interfaces()
            .filter { it.resideInPackage("$rootPackage.application.port.inbound..") }
            .assertTrue(additionalMessage = "Inbound ports must end with 'UseCase' or 'Query'") {
                it.name.endsWith("UseCase") || it.name.endsWith("Query")
            }
    }

    /**
     * Application services orchestrate use cases and must end with `Service`
     * to distinguish them from domain objects and adapters.
     */
    @Test
    fun `application services end with Service`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.resideInPackage("$rootPackage.application.service..") }
            .assertTrue(additionalMessage = "Application services must end with 'Service'") {
                it.name.endsWith("Service")
            }
    }

    // -------------------------------------------------------------------------
    // Structural rules
    // -------------------------------------------------------------------------

    /**
     * Each application service implements exactly one inbound port.
     * A service that handles many use cases is a violation of the single-responsibility principle.
     */
    @Test
    fun `each application service implements exactly one inbound port`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.resideInPackage("$rootPackage.application.service..") }
            .assertTrue(additionalMessage = "Each application service must implement exactly one inbound port") {
                val inboundPortImports = it.containingFile.imports
                    .filter { import -> import.name.startsWith("$rootPackage.application.port.inbound") }
                inboundPortImports.size == 1
            }
    }

    /**
     * Application services must not depend on other application services.
     * Cross-service orchestration leaks into the wrong layer; use outbound ports instead.
     */
    @Test
    fun `application services do not depend on other application services`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.resideInPackage("$rootPackage.application.service..") }
            .assertTrue(additionalMessage = "Application services must not import other application services") {
                it.containingFile.imports
                    .none { import -> import.name.startsWith("$rootPackage.application.service") }
            }
    }

    /**
     * Each inbound adapter is responsible for exactly one use case or query.
     * Adapters that call many ports mix concerns and become harder to test.
     */
    @Test
    fun `inbound adapters depend on at most one inbound port`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.resideInPackage("$rootPackage.adapter.inbound..") }
            .assertTrue(additionalMessage = "Inbound adapters must depend on at most one inbound port") {
                val inboundPortImports = it.containingFile.imports
                    .filter { import -> import.name.startsWith("$rootPackage.application.port.inbound") }
                inboundPortImports.size <= 1
            }
    }
}
