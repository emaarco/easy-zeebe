# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Easy Zeebe is a Spring Boot-based example project demonstrating Zeebe process engine integration. It consists of three Gradle modules and a React frontend:

- **example-service**: The main application with newsletter subscription process implementation
- **common-zeebe**: Shared Zeebe integration utilities and configuration 
- **common-zeebe-test**: Testing utilities for Zeebe process testing
- **frontend**: React TypeScript frontend with Tailwind CSS for newsletter subscription UI

## Development Setup

### Prerequisites
- Java 21 (configured via Gradle toolchain)
- Node.js 18+ and npm (for frontend)
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

### Frontend
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Run type checking
npm run type-check
```

## Architecture

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
- **docker-compose.yml**: Complete infrastructure stack including Camunda Platform 8.8 (unified orchestration), PostgreSQL, and Elasticsearch

### BPMN Processing
- BPMN files located in `src/main/resources/bpmn/`
- Custom Gradle plugin generates Kotlin models from BPMN files
- Process definitions automatically deployed on application startup

## Testing

### Running Tests
```bash
# All tests
./gradlew test

# Specific module tests  
./gradlew :services:example-service:test

# Process integration tests
./gradlew :services:example-service:test --tests "*ProcessTest*"
```

### Test Architecture
- Process tests extend `ZeebeProcessTest` base class
- In-memory Zeebe engine for integration testing
- Job workers automatically registered during tests
- Timer manipulation utilities available via `TimerUtils`

## Infrastructure Access
- **Frontend UI**: http://localhost:3000
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
Use the `gh` CLI for GitHub operations:
```bash
# View repository info
gh repo view owner/repo

# Fetch file contents from GitHub
gh api repos/owner/repo/contents/path/to/file --jq '.content' | base64 -d

# List repository contents
gh api repos/owner/repo/contents/directory
```