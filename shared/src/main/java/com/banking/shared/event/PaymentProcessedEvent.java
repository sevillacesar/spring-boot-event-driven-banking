package com.banking.shared.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentProcessedEvent(
    String eventId,
    String paymentId,
    String sourceAccountId,
    BigDecimal amount,
    String currency,
    String merchant,
    String status,
    String failureReason,
    Instant createdAt
) {
    public PaymentProcessedEvent {
        eventId = (eventId == null) ? UUID.randomUUID().toString() : eventId;
        createdAt = (createdAt == null) ? Instant.now() : createdAt;
    }
}
