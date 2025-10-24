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
    implementation(libs.zeebeSdk)
    implementation(libs.zeebeProcessTest)
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}
