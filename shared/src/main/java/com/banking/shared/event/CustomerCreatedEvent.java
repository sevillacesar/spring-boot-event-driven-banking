package com.banking.shared.event;

import java.time.Instant;
import java.util.UUID;

public record CustomerCreatedEvent(
    String eventId,
    String customerId,
    String firstName,
    String lastName,
    String email,
    String documentNumber,
    Instant createdAt
) {
    public CustomerCreatedEvent {
        eventId = (eventId == null) ? UUID.randomUUID().toString() : eventId;
        createdAt = (createdAt == null) ? Instant.now() : createdAt;
    }
}
