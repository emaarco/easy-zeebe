# 🧪 common-zeebe-test

**Common Zeebe Test** provides shared test configuration for testing BPMN processes with Camunda Platform 8.

This module provides reusable test configuration that can be used across multiple services.

## 📌 What's Included

- **TestProcessEngineConfiguration**: Spring test configuration that ensures `ProcessEngineApi` uses the test `CamundaClient` provided by `@CamundaSpringProcessTest`

## 🔧 How to Use

Include **common-zeebe-test** as a test dependency and import the test configuration:

```kotlin
// In build.gradle.kts
dependencies {
    testImplementation(project(":services:common-zeebe-test"))
}
```

```kotlin
// In your test class
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@CamundaSpringProcessTest
@Import(TestProcessEngineConfiguration::class)
class YourProcessTest {
    // Your tests here
}
```
