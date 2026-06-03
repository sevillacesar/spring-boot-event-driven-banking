package com.banking.customer.event;

import com.banking.customer.domain.Customer;
import com.banking.shared.event.CustomerCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventPublisher.class);
    private static final String BINDING_NAME = "customerCreated-out-0";

    private final StreamBridge streamBridge;

    public CustomerEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishCustomerCreated(Customer customer) {
        var event = new CustomerCreatedEvent(
            null,
            customer.getId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getDocumentNumber(),
            null
        );
        streamBridge.send(BINDING_NAME, event);
        log.info("Published CustomerCreatedEvent: customerId={}", customer.getId());
    }
}
