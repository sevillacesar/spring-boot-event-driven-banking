package com.banking.notification.event;

import com.banking.shared.event.PaymentInitiatedEvent;
import com.banking.shared.event.PaymentProcessedEvent;
import com.banking.shared.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class TransactionEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventHandler.class);

    @Bean
    public Consumer<TransactionEvent> transactionInitiated() {
        return event -> {
            log.info("NOTIFICATION: Transaction {} initiated - {} {} from {} to {}",
                event.transactionId(), event.amount(), event.currency(),
                event.sourceAccountId(), event.targetAccountId());
        };
    }

    @Bean
    public Consumer<PaymentInitiatedEvent> paymentInitiated() {
        return event -> {
            log.info("NOTIFICATION: Payment {} initiated - {} {} at {} (card: ****{})",
                event.paymentId(), event.amount(), event.currency(),
                event.merchant(), event.cardLastFour());
        };
    }

    @Bean
    public Consumer<PaymentProcessedEvent> paymentProcessed() {
        return event -> {
            if ("COMPLETED".equals(event.status())) {
                log.info("NOTIFICATION: Payment {} completed - {} {} at {}",
                    event.paymentId(), event.amount(), event.currency(), event.merchant());
            } else {
                log.warn("NOTIFICATION: Payment {} {} - {} {} at {}: {}",
                    event.paymentId(), event.status(), event.amount(), event.currency(),
                    event.merchant(), event.failureReason());
            }
        };
    }
}
