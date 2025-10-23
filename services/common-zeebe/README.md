# ⚙️ Common Zeebe

**Common Zeebe** is the backbone module for integrating the **Zeebe process engine** into your services. 
It streamlines the connection to Zeebe, making it easy to interact with BPMN workflows 
and manage job workers effortlessly.

This module provides everything you need to seamlessly connect, configure, 
and interact with the Zeebe engine running in your stack.

## 🔧 Key Features

- **Zeebe Client Integration**: Pre-configured Zeebe client for quick and hassle-free connection to your process engine.
- **Worker Base Classes**: Simplifies the creation and management of job workers with reusable base classes.
- **Auto-configurations**: Automatically register your job workers and manage connections to the Zeebe engine.
- **Service Interaction**: Provides utilities and classes to interact with BPMN workflows from your services.

## 🔍 Further Details

You might notice we're not using Zeebe's native **`@JobWorker`** Spring annotation. Here's why:

This example-service uses **[`camunda-process-test-spring`](https://github.com/camunda/camunda/tree/main/testing/camunda-process-test-spring)**
for process testing with Camunda 8.8. This approach allows us to test processes with actual worker implementations
instead of mocking them, which is why we manage worker registration manually with our own worker classes.

### Testing Approach

- Tests use the `@CamundaProcessTest` annotation which provides an in-memory process engine
- Workers are registered manually in tests using the injected `CamundaClient`
- This allows testing with real worker logic without needing to mock the worker behavior
- The `CamundaProcessTestContext` provides utilities for timer manipulation and process control

## 📌 How to Use

Include **common-zeebe** as a dependency in your service modules 
to integrate and interact with the Zeebe engine efficiently.

### Example (Gradle Setup):

```gradle
dependencies {
    implementation(project(":common-zeebe"))
}
```
