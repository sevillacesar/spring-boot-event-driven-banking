package com.banking.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreatePaymentRequest(
    @NotBlank String idempotencyKey,
    @NotBlank String sourceAccountId,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotBlank @Size(min = 3, max = 3) String currency,
    @NotBlank String merchant,
    @NotBlank @Size(min = 4, max = 4) String cardLastFour,
    @NotBlank String cardholderName,
    String description
) {}
