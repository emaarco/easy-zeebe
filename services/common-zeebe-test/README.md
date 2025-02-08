# 🧪 common-zeebe-test

**Common Zeebe Test** is a dedicated module designed to provide a robust setup for **integrative testing** of  BPMN processes, orchestrated by zeebe.

This module serves as a reusable foundation for other projects, like the **example-service**, allowing seamless and efficient testing of BPMN workflows.

## 🔧 Key Features

- **Integrated Process Testing**: Easily test your BPMN processes within a Zeebe environment.
- **Worker  Testing**: Validate the glue code that interacts with your workflows.
- **Reusable Setup**: Designed to be used as a dependency in other modules to simplify testing workflows.

## 📌 How to Use

To streamline process testing, include **common-zeebe-test** as a dependency in any service that contains BPMN processes (such as **example-service**). This setup ensures smooth and efficient testing of both workflows and worker code.

### Example (Gradle Setup):

```gradle
dependencies {
    testImplementation(project(":common-zeebe-test"))
}
```
