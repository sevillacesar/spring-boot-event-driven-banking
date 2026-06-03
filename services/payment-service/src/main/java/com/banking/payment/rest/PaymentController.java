package com.banking.payment.rest;

import com.banking.payment.domain.Payment;
import com.banking.payment.domain.PaymentRepository;
import com.banking.payment.dto.CreatePaymentRequest;
import com.banking.payment.dto.PaymentResponse;
import com.banking.payment.event.PaymentEventPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;

    public PaymentController(PaymentRepository paymentRepository, PaymentEventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        var existing = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return ResponseEntity.ok(PaymentResponse.from(existing.get()));
        }

        var payment = new Payment();
        payment.setIdempotencyKey(request.idempotencyKey());
        payment.setSourceAccountId(request.sourceAccountId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        payment.setMerchant(request.merchant());
        payment.setCardLastFour(request.cardLastFour());
        payment.setCardholderName(request.cardholderName());
        payment.setDescription(request.description());
        payment.setStatus(Payment.PaymentStatus.PENDING);

        payment = paymentRepository.save(payment);
        eventPublisher.publishPaymentInitiated(payment);

        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<PaymentResponse> processPayment(@PathVariable String id) {
        return paymentRepository.findById(id)
            .map(payment -> {
                if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
                    return ResponseEntity.ok(PaymentResponse.from(payment));
                }

                payment.setStatus(Payment.PaymentStatus.PROCESSING);
                paymentRepository.save(payment);

                var approved = payment.getAmount().compareTo(new java.math.BigDecimal("10000")) <= 0;

                if (approved) {
                    payment.setStatus(Payment.PaymentStatus.COMPLETED);
                    payment.setProcessedAt(LocalDateTime.now());
                } else {
                    payment.setStatus(Payment.PaymentStatus.DECLINED);
                    payment.setFailureReason("Exceeds daily authorization limit");
                    payment.setProcessedAt(LocalDateTime.now());
                }

                paymentRepository.save(payment);
                eventPublisher.paymentProcessed(payment);

                return ResponseEntity.ok(PaymentResponse.from(payment));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String id) {
        return paymentRepository.findById(id)
            .map(p -> ResponseEntity.ok(PaymentResponse.from(p)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
            .map(PaymentResponse::from)
            .toList();
    }

    @GetMapping("/account/{accountId}")
    public List<PaymentResponse> getPaymentsByAccount(@PathVariable String accountId) {
        return paymentRepository.findBySourceAccountIdOrderByCreatedAtDesc(accountId).stream()
            .map(PaymentResponse::from)
            .toList();
    }
}
