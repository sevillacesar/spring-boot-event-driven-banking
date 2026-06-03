package com.banking.fraud.service;

import com.banking.shared.event.FraudAlertEvent;
import com.banking.shared.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000.00");

    public FraudAlertEvent analyze(TransactionEvent event) {
        log.info("Analyzing transaction {} for fraud patterns", event.transactionId());

        if (event.amount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            return new FraudAlertEvent(
                null,
                event.transactionId(),
                event.sourceAccountId(),
                event.amount(),
                "High-value transaction exceeds threshold of " + HIGH_VALUE_THRESHOLD,
                FraudAlertEvent.FraudSeverity.MEDIUM,
                null
            );
        }

        if (event.sourceAccountId().equals(event.targetAccountId())) {
            return new FraudAlertEvent(
                null,
                event.transactionId(),
                event.sourceAccountId(),
                event.amount(),
                "Self-transfer detected: source and target accounts are the same",
                FraudAlertEvent.FraudSeverity.HIGH,
                null
            );
        }

        return null;
    }
}
