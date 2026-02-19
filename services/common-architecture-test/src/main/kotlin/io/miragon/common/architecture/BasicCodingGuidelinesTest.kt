package io.miragon.common.architecture

import com.lemonappdev.konsist.api.Konsist
import org.junit.jupiter.api.Test

/**
 * Abstract base class for basic coding guideline tests.
 *
 * Verifies project-wide structural conventions:
 * - All classes reside in a declared package (no default package)
 * - No circular dependencies between architecture layers
 *
 * Concrete implementations must supply [rootPackage] to scope all checks.
 *
 * **Usage:**
 * ```kotlin
 * class MyCodingGuidelinesTest : BasicCodingGuidelinesTest() {
 *     override val rootPackage = "com.example.myservice"
 * }
 * ```
 *
 * For an alternative ArchUnit-based implementation see
 * [spring-guardium-leviosa](https://github.com/emaarco/spring-guardium-leviosa).
 */
abstract class BasicCodingGuidelinesTest {

    /** The root package of the service under test, e.g. `"io.miragon.example"`. */
    abstract val rootPackage: String

    /**
     * Every class within [rootPackage] must declare an explicit package.
     * Classes in the default (unnamed) package are a build smell and break
     * classpath isolation.
     */
    @Test
    fun `all classes have package declarations`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.resideInPackage("$rootPackage..") }
            .assertTrue(additionalMessage = "Every class must declare a package — no class may reside in the default package") {
                it.packagee != null
            }
    }

    /**
     * Verifies that no class in [rootPackage] resides directly in the root package,
     * with the exception of the Spring Boot application entry-point class
     * (detected by checking for the `@SpringBootApplication` annotation).
     *
     * All other classes must be organised in meaningful sub-packages.
     */
    @Test
    fun `no class resides directly in the root package except the application entry point`() {
        Konsist.scopeFromProject()
            .classes()
            .filter { it.packagee?.name == rootPackage }
            .filter { !it.hasAnnotationWithName("SpringBootApplication") }
            .assertTrue(additionalMessage = "Classes must be placed in sub-packages, not directly in '$rootPackage'") {
                false
            }
    }
}
