package com.banking.shared.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountCreatedEvent(
    String eventId,
    String accountId,
    String customerId,
    String accountNumber,
    AccountType accountType,
    BigDecimal initialBalance,
    String currency,
    Instant createdAt
) {
    public AccountCreatedEvent {
        eventId = (eventId == null) ? UUID.randomUUID().toString() : eventId;
        createdAt = (createdAt == null) ? Instant.now() : createdAt;
    }

    public enum AccountType {
        SAVINGS, CHECKING
    }
}
