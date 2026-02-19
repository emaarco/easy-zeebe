package io.miragon.example.architecture

import de.emaarco.konsist.BasicCodingGuidelinesTest
import de.emaarco.konsist.HexagonalArchitectureTest
import org.junit.jupiter.api.Nested

class ExampleServiceArchitectureTest {

    private val rootPackage = "io.miragon.example"

    @Nested
    inner class Architecture : HexagonalArchitectureTest(rootPackage)

    @Nested
    inner class CodingGuidelines : BasicCodingGuidelinesTest(rootPackage)
}
