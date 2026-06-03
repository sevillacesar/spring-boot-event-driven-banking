package com.banking.payment.dto;

import com.banking.payment.domain.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
    String id,
    String sourceAccountId,
    BigDecimal amount,
    String currency,
    String merchant,
    String cardLastFour,
    String cardholderName,
    String description,
    String status,
    String failureReason,
    LocalDateTime createdAt,
    LocalDateTime processedAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
            p.getId(), p.getSourceAccountId(), p.getAmount(),
            p.getCurrency(), p.getMerchant(), p.getCardLastFour(),
            p.getCardholderName(), p.getDescription(),
            p.getStatus().name(), p.getFailureReason(),
            p.getCreatedAt(), p.getProcessedAt()
        );
    }
}
