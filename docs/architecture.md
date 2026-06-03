# Architecture

## Overview

Event-Driven Banking implements a **microservices architecture** with **event-driven communication** via Apache Kafka. Each service owns its data and communicates asynchronously through domain events, enabling loose coupling and independent deployability.

## Principles

- **Database per service**: Each service has its own PostgreSQL instance, preventing tight coupling at the data layer
- **Eventual consistency**: Cross-service state is reconciled through events, not distributed transactions
- **Idempotency**: Event handlers are designed to be idempotent — processing the same event twice produces the same result
- **Observability by default**: Every service exports metrics, traces, and health endpoints

## Communication Patterns

### Event-Driven (Async)

Services publish domain events to Kafka when state changes. Other services consume relevant events and react accordingly.

```
Customer Service          Account Service
     |                         |
     |-- CustomerCreated -->   |
     |    (Kafka topic)        |-- Creates account
     |                         |-- Publishes AccountCreated
```

### REST (Sync)

External clients interact with services through REST APIs. Internal inter-service communication is async-only (no HTTP calls between services).

## Service Responsibilities

### Customer Service (`:8080`)
Manages customer lifecycle. Publishes `CustomerCreatedEvent` when a new customer registers.

### Account Service (`:8081`)
Listens for `CustomerCreatedEvent` and automatically opens a checking account. Manages account state. Publishes `AccountCreatedEvent`.

### Transaction Service (`:8082`)
Handles fund transfers between accounts. Validates balances, applies changes, and publishes `TransactionEvent` for downstream processing.

### Payment Service (`:8084`)
Manages external payment processing. Handles payment initiation, status tracking, and publishes `PaymentInitiatedEvent` / `PaymentProcessedEvent`.

### Notification Service (`:8083`)
Consumes transaction events and logs/sends notifications. Currently implements logging-based notifications, ready to extend to email/SMS channels.

### Fraud Detection Service
Analyzes transactions in real-time for suspicious patterns:
- **High-value threshold**: Flags transactions over $10,000 (MEDIUM severity)
- **Self-transfer detection**: Detects transfers where source == target account (HIGH severity)
- Extensible rule engine for adding new fraud patterns

## Event Flow

```
CustomerCreated --> Account Service
                        |
                        v
                  AccountCreated
                        |
                        v
                  Transaction Initiated (via Transaction Service)
                        |
                   -----+-----
                   |          |
                   v          v
          Fraud Detection   Notification
                   |
                   v
             Fraud Alert
                   |
                   v
             Notification
```

## Observability Stack

| Component | Purpose |
|-----------|---------|
| OpenTelemetry | Distributed tracing across all services |
| Jaeger | Trace visualization and analysis |
| Micrometer | Metrics collection (JVM, HTTP, Kafka) |
| Prometheus | Metrics storage and querying |
| Grafana | Dashboards and alerting |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21, Spring Boot 3.3.5 |
| Event Broker | Apache Kafka 7.6 |
| Serialization | Apache Avro + Schema Registry |
| Database | PostgreSQL 16 (per service) |
| API Docs | SpringDoc OpenAPI 2.6 |
| Testing | JUnit 5, Testcontainers |
| CI/CD | GitHub Actions |
| Containerization | Docker, Docker Compose |
