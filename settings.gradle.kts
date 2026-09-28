pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://central.sonatype.com/repository/maven-snapshots/") {
            content { includeGroupByRegex("io\\.miragon.*") }
        }
    }
}

rootProject.name = "easy-zeebe"

include("services:example-service")
include("services:common-zeebe")
include("services:common-zeebe-test")
include("services:common-architecture-test")
