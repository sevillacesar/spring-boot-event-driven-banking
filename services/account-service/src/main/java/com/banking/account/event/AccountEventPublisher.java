package com.banking.account.event;

import com.banking.account.domain.Account;
import com.banking.shared.event.AccountCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AccountEventPublisher.class);
    private static final String BINDING_NAME = "accountCreated-out-0";

    private final StreamBridge streamBridge;

    public AccountEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishAccountCreated(Account account) {
        var event = new AccountCreatedEvent(
            null,
            account.getId(),
            account.getCustomerId(),
            account.getAccountNumber(),
            AccountCreatedEvent.AccountType.valueOf(account.getAccountType().name()),
            account.getBalance(),
            account.getCurrency(),
            null
        );
        streamBridge.send(BINDING_NAME, event);
        log.info("Published AccountCreatedEvent: accountId={}", account.getId());
    }
}
