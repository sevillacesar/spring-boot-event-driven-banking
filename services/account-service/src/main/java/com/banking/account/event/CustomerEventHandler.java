package com.banking.account.event;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountRepository;
import com.banking.shared.event.CustomerCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;
import java.util.function.Consumer;

@Component
public class CustomerEventHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomerEventHandler.class);
    private final AccountRepository accountRepository;
    private final AccountEventPublisher eventPublisher;

    public CustomerEventHandler(AccountRepository accountRepository, AccountEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Bean
    public Consumer<CustomerCreatedEvent> customerCreated() {
        return event -> {
            log.info("Received CustomerCreatedEvent: customerId={}", event.customerId());

            var account = new Account();
            account.setCustomerId(event.customerId());
            account.setAccountNumber(generateAccountNumber());
            account.setAccountType(Account.AccountType.CHECKING);
            account.setBalance(BigDecimal.ZERO);
            account.setCurrency("USD");
            account.setActive(true);

            accountRepository.save(account);
            eventPublisher.publishAccountCreated(account);

            log.info("Created account {} for customer {}", account.getAccountNumber(), event.customerId());
        };
    }

    private String generateAccountNumber() {
        var random = new Random();
        var sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
