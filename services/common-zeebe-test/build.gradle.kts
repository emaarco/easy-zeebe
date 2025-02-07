plugins {
    alias(libs.plugins.kotlin.jvm)
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
    implementation(project(":services:common-zeebe"))
    implementation(libs.kotlin.jackson)
    implementation(libs.zeebeSdk)
    implementation(libs.zeebeProcessTest)
    implementation(libs.bundles.test)
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
