package com.banking.shared.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FraudAlertEvent(
    String eventId,
    String transactionId,
    String accountId,
    BigDecimal amount,
    String reason,
    FraudSeverity severity,
    Instant createdAt
) {
    public FraudAlertEvent {
        eventId = (eventId == null) ? UUID.randomUUID().toString() : eventId;
        createdAt = (createdAt == null) ? Instant.now() : createdAt;
    }

    public enum FraudSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
