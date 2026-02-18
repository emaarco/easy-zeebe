# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Easy Zeebe is a Spring Boot-based example project demonstrating Zeebe process engine integration.
It consists of three Gradle modules and a React frontend:

- **example-service**: The main application with newsletter subscription process implementation
- **common-zeebe**: Shared Zeebe integration utilities and configuration
- **common-zeebe-test**: Testing utilities for Zeebe process testing

## Development Setup

### Prerequisites

- Java 21 (configured via Gradle toolchain)
- Docker and Docker Compose for infrastructure
- PostgreSQL database (via Docker)

### Starting the Application

1. **Start Infrastructure**:
   ```bash
   cd stack
   docker-compose up -d
   ```
   This starts PostgreSQL, Camunda Platform (unified Zeebe, Operate, Tasklist), and Elasticsearch.

2. **Run Backend**:
   ```bash
   ./gradlew :services:example-service:bootRun
   ```
   Application runs on port 8081.

3. **Run Frontend** (in separate terminal):
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Frontend runs on port 3000 with API proxy configured.

## Build Commands

### Backend

Backend can be built and tested via Gradle commands.
Some important ones are listed below – for the others, consider using the gradle-docs

```bash
# Build entire project
./gradlew build

# Run tests for all modules
./gradlew test

# Run tests for specific module
./gradlew :services:example-service:test
./gradlew :services:common-zeebe:test

# Generate BPMN models from BPMN files
./gradlew :services:example-service:generateBpmnModels

# Clean build artifacts
./gradlew clean
```

## Architecture

When writing code, you must comply with the following architecture guidelines:

### Module Structure

- **Hexagonal Architecture**: example-service follows ports & adapters pattern with clear separation of concerns
- **Domain**: Core business entities (Email, Name, NewsletterSubscription, etc.)
- **Application**: Use cases and services
- **Adapters**:
    - Inbound: REST controllers and Zeebe job workers
    - Outbound: Database persistence and Zeebe process adapters

### Key Components

#### Zeebe Integration

- **ProcessEngineApi**: Main interface for starting processes and sending messages
- **JobWorker**: Annotation-based job workers (implemented via common-zeebe)
- **ZeebeClient**: Auto-configured Spring Boot starter integration

#### Process Testing

- **ZeebeProcessTest**: Base class providing process testing capabilities
- **JobWorkerManager**: Manages job worker lifecycle during tests
- **TimerUtils**: Utilities for handling process timers in tests

### Configuration

- **application.yaml**: Database and application configuration
- **zeebe-application.yaml**: Zeebe-specific configuration loaded via custom property source
- **docker-compose.yml**: Complete infrastructure stack including Camunda Platform 8.8 (unified orchestration),
  PostgreSQL, and Elasticsearch

### BPMN Processing

- BPMN files located in `src/main/resources/bpmn/`
- Custom Gradle plugin generates Kotlin models from BPMN files
- Process definitions automatically deployed on application startup

## Testing

All code should be tested, to ensure quality and maintainability.
Focus should lie on unit tests – integration tests are optional and only created upon request.

### Running Tests

You should always run tests, using the corresponding Gradle tasks.
Prefer running tests combined, then individual, when you need to test multiple files.

### Test Architecture

- Process tests extend `ZeebeProcessTest` base class
- In-memory Zeebe engine for integration testing
- Job workers automatically registered during tests
- Timer manipulation utilities available via `TimerUtils`

## Infrastructure Access

- **Backend API**: http://localhost:8081
- **Zeebe Gateway**: localhost:26500
- **Camunda Web UI** (Operate/Tasklist): http://localhost:8080
- **PostgreSQL**: localhost:5432 (admin/admin)
- **Elasticsearch**: http://localhost:9200

## Key Files

- `services/example-service/src/main/resources/bpmn/newsletter.bpmn`: Main process definition
- `services/example-service/src/main/kotlin/io/miragon/example/ExampleApplication.kt`: Application entry point
- `services/common-zeebe/src/main/kotlin/io/miragon/common/zeebe/engine/ProcessEngineApi.kt`: Core Zeebe API
- `frontend/src/App.tsx`: Main React frontend component with newsletter subscription form
- `frontend/src/services/api.ts`: API client for backend communication

## Working with GitHub

When asked to work with GitHub, always use the `gh` CLI if available.

## Skills

This repo ships with custom Claude Code skills in `.claude/skills/`. When a task matches an available skill, use it
instead of implementing manually.

| Skill                      | Command                       | When to use                                                  |
|----------------------------|-------------------------------|--------------------------------------------------------------|
| create-ticket              | `/create-ticket`              | Create a GitHub issue (feature, bug, or refactor)            |
| create-worker              | `/create-worker`              | Generate @JobWorker classes (inbound adapters) only          |
| create-process-adapter     | `/create-process-adapter`     | Generate a process out-adapter (outbound adapter) only       |
| create-rest-controller     | `/create-rest-controller`     | Generate a REST controller (inbound adapter)                 |
| create-persistence-adapter | `/create-persistence-adapter` | Generate a JPA persistence adapter (outbound adapter)        |
| test-worker                | `/test-worker`                | Generate unit tests for a Zeebe job worker                   |
| test-process-adapter       | `/test-process-adapter`       | Generate unit tests for a Zeebe process out-adapter          |
| test-process               | `/test-process`               | Generate process integration tests                           |
| automate-process           | `/automate-process`           | Generate full hexagonal glue-code (workers, ports, services) |
| create-adr                 | `/create-adr`                 | Write a new Architectural Decision Record in docs/adr/       |
| test-rest-adapter          | `/test-rest-adapter`          | Generate tests for a REST controller (inbound adapter)       |
| test-persistence-adapter   | `/test-persistence-adapter`   | Generate tests for a JPA persistence adapter                 |

## Subagents

This repo also ships a custom Claude Code subagent in `.claude/agents/`. Subagents run in their own isolated context
with restricted tool access and are invoked automatically by Claude when relevant, or explicitly on request.

| Subagent         | When to use                                                                                                                                              |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `review-process` | Review a BPMN model and its glue-code for consistency. Ask: "review the newsletter process" or "use the review-process subagent to audit the newsletter" |