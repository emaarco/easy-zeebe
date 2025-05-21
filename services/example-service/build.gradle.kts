import io.github.emaarco.bpmn.adapter.GenerateBpmnModelsTask
import io.github.emaarco.bpmn.domain.shared.OutputLanguage
import io.github.emaarco.bpmn.domain.shared.ProcessEngine

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.bpmnToCode)
}

group = "de.emaarco.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.defaultService)
    implementation(libs.bundles.database)
    implementation(project(":services:common-zeebe"))
    testImplementation(libs.bundles.test)
    testImplementation(libs.zeebeProcessTest)
    testImplementation(project(":services:common-zeebe-test"))
}

tasks.register<GenerateBpmnModelsTask>("generateBpmnModels") {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/bpmn/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "de.emaarco.example.adapter.process"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
    useVersioning = false
}

tasks.test {
    useJUnitPlatform()
}
