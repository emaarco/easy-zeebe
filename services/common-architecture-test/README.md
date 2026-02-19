# 🏛️ common-architecture-test

**Common Architecture Test** provides reusable, abstract test base classes that enforce hexagonal architecture
conventions at build time. Services extend these classes to get automatic layer-boundary and naming-convention
checks wired into their regular test runs.

## ❓ Why architecture tests?

Hexagonal architecture conventions are easy to violate silently.
An adapter that imports an application service directly, a port interface that becomes a concrete class,
or a naming inconsistency — none of these break compilation or unit tests.
They only become visible much later, when the codebase has grown enough that the violation has spread.

Architecture tests turn those silent violations into **build failures**.
Every time `./gradlew test` runs, the layer boundaries and naming rules are verified automatically.

## 📌 What's included

### `BasicCodingGuidelinesTest`

Foundational checks that apply to any well-structured Kotlin service:

| Rule                                   | Description                             |
|----------------------------------------|-----------------------------------------|
| All classes have a package declaration | No class ends up in the default package |
| No circular package dependencies       | Package graph is a DAG                  |

### `HexagonalArchitectureTest`

Hexagonal architecture-specific checks, grouped by concern:

**Layer dependency rules** (checked via import names):

| Rule              | Description                                                                                    |
|-------------------|------------------------------------------------------------------------------------------------|
| Domain isolation  | `domain` classes only import from the domain itself and `kotlin`/`java` stdlib                 |
| Port purity       | `application.port` interfaces only import from the domain                                      |
| Service scope     | `application.service` classes only import from `domain`, `application.port`, and standard libs |
| Adapter isolation | `adapter` classes never import from `application.service`                                      |

**Port rules:**

| Rule                 | Description                                                                           |
|----------------------|---------------------------------------------------------------------------------------|
| Ports are interfaces | All top-level declarations in `application.port` packages are interfaces, not classes |

**Naming rules:**

| Rule               | Description                   |
|--------------------|-------------------------------|
| Inbound port names | End with `UseCase` or `Query` |
| Service names      | End with `Service`            |

**Structural rules:**

| Rule                           | Description                                                                  |
|--------------------------------|------------------------------------------------------------------------------|
| Single-port services           | Each application service implements exactly one inbound port                 |
| No service-to-service coupling | Application services do not import other application services                |
| Focused adapters               | Each inbound adapter depends on at most one inbound port via its constructor |

## 🔧 How to use

### 1. Add the dependency

```kotlin
// In your service's build.gradle.kts
dependencies {
    testImplementation(project(":services:common-architecture-test"))
}
```

### 2. Create a concrete subclass

```kotlin
// src/test/kotlin/com/example/myservice/architecture/MyServiceCodingGuidelinesTest.kt
class MyServiceCodingGuidelinesTest : BasicCodingGuidelinesTest() {
    override val rootPackage = "com.example.myservice"
}

// src/test/kotlin/com/example/myservice/architecture/MyServiceHexagonalArchitectureTest.kt
class MyServiceHexagonalArchitectureTest : HexagonalArchitectureTest() {
    override val rootPackage = "com.example.myservice"
}
```

That's it. The inherited `@Test` methods run automatically as part of your service's test suite.

## 🔀 Alternatives

This module uses [Konsist](https://docs.konsist.lemonappdev.com/) — a Kotlin-first static source-analysis
library that works directly on `.kt` source files without needing compiled bytecode.

For a JVM-agnostic alternative built on [ArchUnit](https://www.archunit.org/) (works with Java, Kotlin, and
other JVM languages), see
[spring-gardium-leviosa](https://github.com/emaarco/spring-gardium-leviosa).
