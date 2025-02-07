plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.springframework)
    alias(libs.plugins.spring.dependency)
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

tasks.test {
    useJUnitPlatform()
}
