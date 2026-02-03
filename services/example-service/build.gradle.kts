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
    alias(libs.plugins.gradleRetryTesting)
}

group = "io.miragon.example"
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
    testImplementation("com.h2database:h2")
}

tasks.named<GenerateBpmnModelsTask>("generateBpmnModelApi") {
    baseDir = projectDir.toString()
    filePattern = "src/main/resources/bpmn/*.bpmn"
    outputFolderPath = "$projectDir/src/main/kotlin"
    packagePath = "io.miragon.example.adapter.process"
    outputLanguage = OutputLanguage.KOTLIN
    processEngine = ProcessEngine.ZEEBE
    useVersioning = false
}

tasks.test {
    useJUnitPlatform()
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

/**
 * Use this for more reliable zeebe-test
 * They sometimes cause issues, because of their async nature
 */
tasks.test {
    retry {
        maxRetries.set(3)
        maxFailures.set(3)
        failOnPassedAfterRetry.set(false)
    }
}
