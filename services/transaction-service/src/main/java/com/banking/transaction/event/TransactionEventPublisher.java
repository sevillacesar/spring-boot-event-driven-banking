package com.banking.transaction.event;

import com.banking.shared.event.TransactionEvent;
import com.banking.transaction.domain.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);
    private static final String BINDING_NAME = "transactionInitiated-out-0";

    private final StreamBridge streamBridge;

    public TransactionEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishTransactionInitiated(Transaction transaction) {
        var event = new TransactionEvent(
            null,
            transaction.getId(),
            transaction.getSourceAccountId(),
            transaction.getTargetAccountId(),
            transaction.getAmount(),
            transaction.getCurrency(),
            TransactionEvent.TransactionType.valueOf(transaction.getType().name()),
            TransactionEvent.TransactionStatus.PENDING,
            transaction.getDescription(),
            null
        );
        streamBridge.send(BINDING_NAME, event);
        log.info("Published TransactionInitiated: transactionId={}", transaction.getId());
    }
}
