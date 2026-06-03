package com.banking.transaction.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    List<Transaction> findBySourceAccountId(String sourceAccountId);
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
}
