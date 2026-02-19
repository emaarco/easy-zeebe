plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.miragon.common"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.konsist)
    implementation(libs.junit.jupiter.api)
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
