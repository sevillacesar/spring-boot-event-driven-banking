package com.banking.customer.dto;

import com.banking.customer.domain.Customer;
import java.time.LocalDateTime;

public record CustomerResponse(
    String id,
    String firstName,
    String lastName,
    String email,
    String documentNumber,
    String phone,
    LocalDateTime createdAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
            customer.getId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getDocumentNumber(),
            customer.getPhone(),
            customer.getCreatedAt()
        );
    }
}
