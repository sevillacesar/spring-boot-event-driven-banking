package com.banking.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findBySourceAccountIdOrderByCreatedAtDesc(String sourceAccountId);
    List<Payment> findByMerchantOrderByCreatedAtDesc(String merchant);
}
