package com.banking.fraud;

import com.banking.fraud.service.FraudDetectionService;
import com.banking.shared.event.FraudAlertEvent;
import com.banking.shared.event.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FraudDetectionServiceUnitTest {

    private FraudDetectionService service;

    @BeforeEach
    void setUp() {
        service = new FraudDetectionService();
    }

    @Test
    void shouldFlagHighAmountTransaction() {
        var event = new TransactionEvent(
            null, "txn-1", "src-1", "tgt-1",
            new BigDecimal("50000"), "USD",
            TransactionEvent.TransactionType.TRANSFER,
            TransactionEvent.TransactionStatus.PENDING,
            "Large transfer", null);

        var alert = service.analyze(event);

        assertThat(alert).isNotNull();
        assertThat(alert.severity()).isEqualTo(FraudAlertEvent.FraudSeverity.MEDIUM);
        assertThat(alert.reason()).containsIgnoringCase("high-value");
    }

    @Test
    void shouldPassTransactionJustUnderThreshold() {
        var event = new TransactionEvent(
            null, "txn-1b", "src-1", "tgt-1",
            new BigDecimal("9999.99"), "USD",
            TransactionEvent.TransactionType.TRANSFER,
            TransactionEvent.TransactionStatus.PENDING,
            "Almost threshold", null);

        assertThat(service.analyze(event)).isNull();
    }

    @Test
    void shouldFlagSelfTransfer() {
        var event = new TransactionEvent(
            null, "txn-2", "same-account", "same-account",
            new BigDecimal("100"), "USD",
            TransactionEvent.TransactionType.TRANSFER,
            TransactionEvent.TransactionStatus.PENDING,
            "Self transfer", null);

        var alert = service.analyze(event);

        assertThat(alert).isNotNull();
        assertThat(alert.severity()).isEqualTo(FraudAlertEvent.FraudSeverity.HIGH);
        assertThat(alert.reason()).containsIgnoringCase("Self-transfer");
    }

    @Test
    void shouldPassNormalTransaction() {
        var event = new TransactionEvent(
            null, "txn-4", "src-3", "tgt-3",
            new BigDecimal("250"), "USD",
            TransactionEvent.TransactionType.TRANSFER,
            TransactionEvent.TransactionStatus.PENDING,
            "Normal payment", null);

        var alert = service.analyze(event);

        assertThat(alert).isNull();
    }

    @Test
    void shouldFlagHighAmountEvenForSelfTransfer() {
        var event = new TransactionEvent(
            null, "txn-5", "account-x", "account-x",
            new BigDecimal("50000"), "USD",
            TransactionEvent.TransactionType.TRANSFER,
            TransactionEvent.TransactionStatus.PENDING,
            "Large self-transfer", null);

        var alert = service.analyze(event);

        assertThat(alert).isNotNull();
        assertThat(alert.severity()).isEqualTo(FraudAlertEvent.FraudSeverity.MEDIUM);
        assertThat(alert.reason()).containsIgnoringCase("high-value");
    }
}
