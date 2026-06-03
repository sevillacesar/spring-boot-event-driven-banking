# Event Catalog

All events are defined in the `shared` module under `com.banking.shared.event`. They are published to Kafka topics and consumed by downstream services.

## CustomerCreatedEvent

Published when a new customer registers.

**Topic**: `customer.created`

```java
public record CustomerCreatedEvent(
    String eventId,
    String customerId,
    String firstName,
    String lastName,
    String email,
    String documentNumber,
    String phone,
    Instant createdAt
)
```

**Produced by**: Customer Service
**Consumed by**: Account Service

---

## AccountCreatedEvent

Published when a new account is created (automatically after customer creation).

**Topic**: `account.created`

```java
public record AccountCreatedEvent(
    String eventId,
    String accountId,
    String accountNumber,
    String customerId,
    String accountType,  // SAVINGS | CHECKING
    String currency,
    BigDecimal balance,
    Instant createdAt
)
```

**Produced by**: Account Service
**Consumed by**: (Future: Notification Service)

---

## TransactionEvent

Published when a transfer is initiated.

**Topic**: `transaction.initiated`

```java
public record TransactionEvent(
    String eventId,
    String transactionId,
    String sourceAccountId,
    String targetAccountId,
    BigDecimal amount,
    String currency,
    TransactionType type,       // TRANSFER | DEPOSIT | WITHDRAWAL
    TransactionStatus status,   // PENDING | APPROVED | REJECTED
    String description,
    Instant createdAt
)
```

**Produced by**: Transaction Service
**Consumed by**: Fraud Detection, Notification

---

## FraudAlertEvent

Published when fraud detection finds a suspicious transaction.

**Topic**: `fraud.alert`

```java
public record FraudAlertEvent(
    String eventId,
    String transactionId,
    String accountId,
    BigDecimal amount,
    String reason,
    FraudSeverity severity,  // LOW | MEDIUM | HIGH
    Instant createdAt
)
```

**Produced by**: Fraud Detection Service
**Consumed by**: Notification Service

---

## PaymentInitiatedEvent

Published when an external payment is initiated.

**Topic**: `payment.initiated`

```java
public record PaymentInitiatedEvent(
    String eventId,
    String paymentId,
    String sourceAccountId,
    String targetAccountId,
    BigDecimal amount,
    String currency,
    String description,
    Instant createdAt
)
```

**Produced by**: Payment Service
**Consumed by**: (Future services)

---

## PaymentProcessedEvent

Published when a payment completes processing.

**Topic**: `payment.processed`

```java
public record PaymentProcessedEvent(
    String eventId,
    String paymentId,
    String status,  // COMPLETED | FAILED
    String failureReason,
    Instant processedAt
)
```

**Produced by**: Payment Service
**Consumed by**: (Future services)

---

## Event Topic Map

| Topic | Producer | Consumers | Schema |
|-------|----------|-----------|--------|
| `customer.created` | Customer Service | Account Service | `CustomerCreatedEvent` |
| `account.created` | Account Service | — | `AccountCreatedEvent` |
| `transaction.initiated` | Transaction Service | Fraud Detection, Notification | `TransactionEvent` |
| `fraud.alert` | Fraud Detection | Notification | `FraudAlertEvent` |
| `payment.initiated` | Payment Service | — | `PaymentInitiatedEvent` |
| `payment.processed` | Payment Service | — | `PaymentProcessedEvent` |
