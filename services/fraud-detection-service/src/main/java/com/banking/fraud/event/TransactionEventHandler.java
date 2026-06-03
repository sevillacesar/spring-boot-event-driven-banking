package com.banking.fraud.event;

import com.banking.fraud.service.FraudDetectionService;
import com.banking.shared.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class TransactionEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventHandler.class);
    private static final String BINDING_NAME = "fraudAlert-out-0";

    private final FraudDetectionService fraudDetectionService;
    private final StreamBridge streamBridge;

    public TransactionEventHandler(FraudDetectionService fraudDetectionService, StreamBridge streamBridge) {
        this.fraudDetectionService = fraudDetectionService;
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<TransactionEvent> transactionInitiated() {
        return event -> {
            log.info("Fraud detection analyzing transaction: {}", event.transactionId());
            var alert = fraudDetectionService.analyze(event);
            if (alert != null) {
                streamBridge.send(BINDING_NAME, alert);
                log.warn("Fraud alert published for transaction: {}", event.transactionId());
            } else {
                log.info("Transaction {} passed fraud checks", event.transactionId());
            }
        };
    }
}
