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

This example-service relies on the plain `zeebe-process-test` framework for testing, 
as it's (in my opinion) the only library offering sufficient functionality for **Zeebe 8.6**.
However, this framework does **not** support Spring-based tests, 
which is why we manage worker registration manually with our own worker classes.

### Why are we using plain zeebe-process-test?

- **[`spring-boot-starter-camunda-test`](https://mvnrepository.com/artifact/io.camunda.spring/spring-boot-starter-camunda-test)** is no longer supported for 8.6.
- **[`camunda-process-test-spring`](https://github.com/camunda/camunda/tree/main/testing/camunda-process-test-spring)** aims to replace both `spring-boot-starter-camunda-test` and `zeebe-process-test` in the future, but it's currently in **alpha** and has a limited feature set.

We might revert to Spring-based testing once future versions provide better support. 
**Stay tuned for updates!** 🚀

## 📌 How to Use

Include **common-zeebe** as a dependency in your service modules 
to integrate and interact with the Zeebe engine efficiently.

### Example (Gradle Setup):

```gradle
dependencies {
    implementation(project(":common-zeebe"))
}
```
