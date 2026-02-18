# 0001. Use Hexagonal Architecture for example-service

Date: 2026-02-18

## Status

Accepted

## Context

The `example-service` module integrates three distinct external systems:
a PostgreSQL database (via JPA),
a Zeebe process engine (via Camunda Spring Boot Starter),
and an HTTP REST API for inbound calls.

Without structural boundaries, coupling between these adapters and business logic tends to grow over time,
making individual components difficult to test in isolation and harder to replace or evolve independently.

The team wants:

- Business rules that can be tested without starting Zeebe, a database, or a full Spring context.
- The ability to swap the Zeebe adapter for a different process engine without touching domain or application code.
- A clear, consistent convention for where new code lives so contributors can orient quickly.

## Decision

We will structure `example-service` using Hexagonal Architecture (Ports and Adapters) with the following package
layout:

| Layer             | Package                                        | Contents                                             |
|-------------------|------------------------------------------------|------------------------------------------------------|
| Domain            | `io.miragon.example.domain`                    | Core entities and value objects                      |
| Application       | `io.miragon.example.application.service`       | Use-case orchestration (implements inbound ports)    |
| Inbound ports     | `io.miragon.example.application.port.inbound`  | Use-case interfaces driven by external actors        |
| Outbound ports    | `io.miragon.example.application.port.outbound` | Repository and process interfaces                    |
| Inbound adapters  | `io.miragon.example.adapter.inbound`           | REST controllers and Zeebe job workers               |
| Outbound adapters | `io.miragon.example.adapter.outbound`          | JPA persistence adapters and Zeebe process adapter   |
| Process API       | `io.miragon.example.adapter.process`           | Generated ProcessApi constants (bpmn-to-code output) |

All cross-layer dependencies point inward:
adapters depend on ports;
ports depend on domain types;
the domain has no external dependencies.

## Consequences

### Positive

- Each layer can be tested in isolation using mocks for the adjacent ports (see `AbortRegistrationWorkerTest.kt` and
  `NewsletterSubscriptionProcessAdapterTest.kt`).
- Adding a new inbound channel (e.g. a Kafka consumer) requires creating a new inbound adapter only; application and
  domain code remain untouched.
- The skills `/automate-process-hexagonal` and `/test-worker` can rely on a predictable package structure when
  generating code.

### Negative

- More boilerplate for simple operations: a new use case requires an interface in `port/inbound/`, a service in
  `application/service/`, and an adapter method. This overhead is acceptable given the project's teaching and template
  purpose.
- Developers unfamiliar with hexagonal architecture face a steeper initial learning curve compared to a layered (MVC)
  structure.

### Neutral

- Generated ProcessApi files (`adapter/process/`) sit inside the adapter layer. They contain only constants derived
  from the BPMN model and no logic, so they do not violate the inward-dependency rule despite being generated
  artefacts.
- The `frontend` and `common-zeebe` modules are not subject to this ADR; they follow their own structural conventions.
