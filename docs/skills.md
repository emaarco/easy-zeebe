# Skills Index

easy-zeebe ships with AI-powered skills and subagents that handle the most repetitive parts of Zeebe development.
All skills follow the [Agent Skills](https://agentskills.io/home) open standard —
they are not tied to any single AI agent and can run in any compatible environment.

Skills are split into two categories:
**Zeebe skills** are the core value — reusable workflows for any Zeebe/Camunda project.
**Supporting skills** were built to scaffold this repo and serve as examples for hexagonal architecture adapters.

## Install

```bash
# Install all skills at once
npx skills add https://github.com/miragon/easy-zeebe

# Or pick individual skills
npx skills add https://github.com/miragon/easy-zeebe/tree/main/.claude/skills/create-worker
```

## Zeebe Skills

Skills covering the full Zeebe development lifecycle — from scaffolding workers to testing and reviewing processes.

| Skill | Description |
|---|---|
| [`create-worker`](../.claude/skills/create-worker/SKILL.md) | Generate or update a `@JobWorker` class (inbound adapter) for a BPMN service task |
| [`create-process-adapter`](../.claude/skills/create-process-adapter/SKILL.md) | Generate or update a process out-adapter (outbound adapter) for `startProcess`, `sendMessage`, and `sendSignal` |
| [`automate-process`](../.claude/skills/automate-process/SKILL.md) | Scaffold full hexagonal glue-code (workers, ports, services, process adapter) for a BPMN process |
| [`test-worker`](../.claude/skills/test-worker/SKILL.md) | Generate a unit test for a Zeebe `@JobWorker` class |
| [`test-process-adapter`](../.claude/skills/test-process-adapter/SKILL.md) | Generate a unit test for a Zeebe process out-adapter |
| [`test-process`](../.claude/skills/test-process/SKILL.md) | Generate `@CamundaSpringProcessTest` integration tests for a BPMN process |
| [`review-process`](../.claude/skills/review-process/SKILL.md) | Audit a BPMN process and its glue-code for consistency and coverage |

## Supporting Skills

Generic hexagonal-architecture helpers included as inspiration for your own projects.

| Skill | Description |
|---|---|
| [`create-rest-controller`](../.claude/skills/create-rest-controller/SKILL.md) | Scaffold or update a Spring `@RestController` with nested DTOs and mapping |
| [`create-persistence-adapter`](../.claude/skills/create-persistence-adapter/SKILL.md) | Scaffold a complete JPA persistence adapter (entity, repository, mapper, adapter) |
| [`create-adr`](../.claude/skills/create-adr/SKILL.md) | Write a new Architecture Decision Record in MADR format |
| [`create-ticket`](../.claude/skills/create-ticket/SKILL.md) | Create or update a GitHub issue with structured templates |
| [`test-rest-adapter`](../.claude/skills/test-rest-adapter/SKILL.md) | Generate `@WebMvcTest` tests with `MockMvc` and `@MockkBean` |
| [`test-persistence-adapter`](../.claude/skills/test-persistence-adapter/SKILL.md) | Generate `@DataJpaTest` tests with H2 and `TestEntityManager` |
| [`test-application-service`](../.claude/skills/test-application-service/SKILL.md) | Generate pure mockk unit tests for application-layer services |

## Subagents

Subagents run in their own isolated context with restricted tool access,
and are invoked automatically by the agent when relevant — or explicitly on request.

| Subagent | Description |
|---|---|
| [`review-process`](../.claude/agents/review-process.md) | Audits a BPMN model and its glue-code for consistency and test coverage against the styleguide |
