package com.banking.account.rest;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountRepository;
import com.banking.account.event.AccountEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountEventPublisher eventPublisher;
    private final Random random = new Random();

    public AccountController(AccountRepository accountRepository, AccountEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable String id) {
        return accountRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public List<Account> getAccountsByCustomer(@PathVariable String customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Map<String, Object> request) {
        var account = new Account();
        account.setCustomerId((String) request.get("customerId"));
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(
            request.containsKey("accountType")
                ? Account.AccountType.valueOf((String) request.get("accountType"))
                : Account.AccountType.CHECKING
        );
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency((String) request.getOrDefault("currency", "USD"));
        account.setActive(true);

        accountRepository.save(account);
        eventPublisher.publishAccountCreated(account);

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    private String generateAccountNumber() {
        var sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable String id, @RequestBody Map<String, Object> request) {
        return accountRepository.findById(id)
            .map(account -> {
                var key = (String) request.get("idempotencyKey");
                if (key != null && key.equals(account.getLastDepositKey())) {
                    return ResponseEntity.ok(account);
                }

                account.setBalance(account.getBalance().add(new BigDecimal(request.get("amount").toString())));
                if (key != null) {
                    account.setLastDepositKey(key);
                }
                accountRepository.save(account);
                return ResponseEntity.ok(account);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
