plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.miragon.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.konsist)
    implementation(libs.spring.test)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}
