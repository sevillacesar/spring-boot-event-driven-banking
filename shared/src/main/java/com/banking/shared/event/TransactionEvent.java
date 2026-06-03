package com.banking.shared.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionEvent(
    String eventId,
    String transactionId,
    String sourceAccountId,
    String targetAccountId,
    BigDecimal amount,
    String currency,
    TransactionType type,
    TransactionStatus status,
    String description,
    Instant createdAt
) {
    public TransactionEvent {
        eventId = (eventId == null) ? UUID.randomUUID().toString() : eventId;
        createdAt = (createdAt == null) ? Instant.now() : createdAt;
    }

    public enum TransactionType {
        TRANSFER, DEPOSIT, WITHDRAWAL
    }

    public enum TransactionStatus {
        PENDING, APPROVED, REJECTED
    }
}
