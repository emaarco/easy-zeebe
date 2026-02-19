package de.emaarco.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BasicCodingGuidelinesTest(
    val pathFromRoot: String,
) {

    @Test
    fun `each class has package declaration`() {
        Konsist
            .scopeFromProject(pathFromRoot)
            .classesAndInterfacesAndObjects()
            .assertTrue { it.resideInPackage("..") }
    }

    @Test
    fun `no wildcard imports`() {
        Konsist.scopeFromProject().imports.assertFalse {
            it.isWildcard && !it.name.startsWith("java.util")
        }
    }
}
