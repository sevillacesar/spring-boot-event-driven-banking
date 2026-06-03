package com.banking.payment.event;

import com.banking.payment.domain.Payment;
import com.banking.shared.event.PaymentInitiatedEvent;
import com.banking.shared.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final StreamBridge streamBridge;

    public PaymentEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishPaymentInitiated(Payment payment) {
        var event = new PaymentInitiatedEvent(
            null, payment.getId(), payment.getSourceAccountId(),
            payment.getAmount(), payment.getCurrency(), payment.getMerchant(),
            payment.getCardLastFour(), payment.getDescription(), null
        );
        streamBridge.send("paymentInitiated-out-0", event);
        log.info("Published PaymentInitiated: paymentId={}", payment.getId());
    }

    public void paymentProcessed(Payment payment) {
        var event = new PaymentProcessedEvent(
            null, payment.getId(), payment.getSourceAccountId(),
            payment.getAmount(), payment.getCurrency(), payment.getMerchant(),
            payment.getStatus().name(), payment.getFailureReason(), null
        );
        streamBridge.send("paymentProcessed-out-0", event);
        log.info("Published PaymentProcessed: paymentId={} status={}", payment.getId(), payment.getStatus());
    }
}
