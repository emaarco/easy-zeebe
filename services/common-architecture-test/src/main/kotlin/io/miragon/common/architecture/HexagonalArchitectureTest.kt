package de.emaarco.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoInterfaceDeclaration
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

abstract class HexagonalArchitectureTest(
    val rootPackage: String,
) {

    private val inboundPortPackage = "$rootPackage.application.port.inbound"

    @Test
    fun `hexagonal architecture should be respected`() {
        Konsist
            .scopeFromProject()
            .assertArchitecture {
                val domainLayer = Layer("Domain", "$rootPackage.domain..")
                val inPortsLayer = Layer("In-Ports", "$rootPackage.application.port.inbound..")
                val outPortsLayer = Layer("Out-Ports", "$rootPackage.application.port.outbound..")
                val inAdaptersLayer = Layer("In-Adapters", "$rootPackage.adapter.inbound..")
                val outAdaptersLayer = Layer("Out-Adapters", "$rootPackage.adapter.outbound..")
                val applicationLayer = Layer("Application", "$rootPackage.application.service..")

                domainLayer.dependsOnNothing()
                inPortsLayer.dependsOn(domainLayer)
                outPortsLayer.dependsOn(domainLayer)
                inAdaptersLayer.dependsOn(domainLayer, inPortsLayer)
                outAdaptersLayer.dependsOn(domainLayer, outPortsLayer)
                applicationLayer.dependsOn(domainLayer, inPortsLayer, outPortsLayer)
            }
    }

    @Nested
    inner class GeneralPortTests {

        @Test
        fun `all ports should be interfaces`() {
            Konsist
                .scopeFromPackage("$rootPackage.application.port..")
                .classesAndInterfacesAndObjects(includeNested = false, includeLocal = false)
                .assertTrue { it is KoInterfaceDeclaration }
        }

        @Test
        fun `in ports should either be use-cases or queries`() {
            Konsist
                .scopeFromProject("..application.port.inbound..")
                .classesAndInterfacesAndObjects(includeNested = false, includeLocal = false)
                .assertTrue {
                    it.hasInterfaceWithName("UseCase") || it.hasInterfaceWithName("Query")
                }
        }
    }

    @Nested
    inner class ApplicationTests {

        @Test
        fun `application services are classes`() {
            Konsist
                .scopeFromProject("..application.service..")
                .files
                .assertTrue { it is KoClassDeclaration }
        }

        @Test
        fun `application services should be named with Service suffix`() {
            Konsist
                .scopeFromProject("..application.service..")
                .classesAndInterfacesAndObjects(includeNested = false, includeLocal = false)
                .assertTrue { it.hasNameEndingWith("Service") }
        }

        @Test
        fun `application service should implement exactly one use-case`() {
            Konsist
                .scopeFromProject("..application.service..")
                .classesAndInterfacesAndObjects(includeNested = false, includeLocal = false)
                .assertTrue { service ->
                    val parentInterfaces = service.parentInterfaces()
                    val useCases = parentInterfaces.filter { it.resideInPackage("..application.port.inbound") }
                    useCases.size == 1
                }
        }

        @Test
        fun `application service should not depends on other application services`() {
            Konsist
                .scopeFromPackage("..application.service..")
                .classes()
                .assertTrue { service ->
                    val parameters = service.primaryConstructor?.parameters ?: emptyList()
                    parameters.none { parameter ->
                        val isService = parameter.name.endsWith("Service")
                        val isDomainService = parameter.name.endsWith("DomainService")
                        isService && !isDomainService
                    }
                }
        }
    }

    @Nested
    inner class InAdapterTests {

        @Test
        fun `in adapters should only offer one use-case or query`() {
            Konsist
                .scopeFromPackage("$rootPackage.adapter.inbound..")
                .files
                .filterNot { it.hasPackage("$rootPackage.adapter.inbound.shared") }
                .assertTrue { adapter ->
                    val allImports = adapter.imports.map { it.name }
                    val importsOfUseCases = allImports.filter { it.startsWith(inboundPortPackage) }
                    importsOfUseCases.size <= 1
                }
        }
    }
}
