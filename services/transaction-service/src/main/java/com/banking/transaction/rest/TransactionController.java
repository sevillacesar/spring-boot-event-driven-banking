package com.banking.transaction.rest;

import com.banking.transaction.domain.Transaction;
import com.banking.transaction.domain.TransactionRepository;
import com.banking.transaction.event.TransactionEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

    public TransactionController(TransactionRepository transactionRepository, TransactionEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> createTransfer(@RequestBody Map<String, Object> request) {
        var idempotencyKey = (String) request.get("idempotencyKey");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "idempotencyKey is required"));
        }

        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }

        var transaction = new Transaction();
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setSourceAccountId((String) request.get("sourceAccountId"));
        transaction.setTargetAccountId((String) request.get("targetAccountId"));
        transaction.setAmount(new BigDecimal(request.get("amount").toString()));
        transaction.setCurrency((String) request.getOrDefault("currency", "USD"));
        transaction.setType(Transaction.TransactionType.TRANSFER);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setDescription((String) request.get("description"));

        transaction = transactionRepository.save(transaction);
        eventPublisher.publishTransactionInitiated(transaction);

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String id) {
        return transactionRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountId}")
    public List<Transaction> getTransactionsByAccount(@PathVariable String accountId) {
        return transactionRepository.findBySourceAccountId(accountId);
    }
}
