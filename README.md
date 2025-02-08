# 🚀 Easy Zeebe

Welcome to **Easy Zeebe** – a helpful example setup for using **Zeebe** as a process engine to orchestrate BPMN workflows! This repository demonstrates how to integrate Zeebe into a Spring-based service and offers a comprehensive testing setup, including an example of an integrated zeebe-process-test.

Whether you're just getting started with Zeebe or looking for a reliable testing setup, **easy-zeebe** has got you covered! 💡

## 📚 Overview

The repository is organized as follows:

- **`/services`**: Contains the zeebe-example-service, including modules for integrating and testing Zeebe.
- **`/stack`**: Infrastructure setup (like Zeebe and other dependencies) to run the examples.
- **`/bruno`**: Example requests to interact with the service using [Bruno](https://www.usebruno.com/).
- **`/run`**: IntelliJ run configurations to easily start the service.

## 🔧 Getting Started

Follow these steps to get the service up and running:

1. **Start the Zeebe Stack**

   Navigate to the `/stack` directory and start the infrastructure using Docker Compose:

   ```bash
   docker-compose up -d
   ```

2. **Run the Application**

   The project is a **Gradle**-based Spring service. While you can run it via command line, the easiest way is using **IntelliJ**:

    - Open the project in IntelliJ.
    - Go to the **`/run`** folder.
    - Use the provided run configurations to start the application.

3. **Interact with the API**

   Use the requests provided in the **`/bruno`** folder to interact with the service. If you don’t have Bruno yet, you can download it [here](https://www.usebruno.com/).
