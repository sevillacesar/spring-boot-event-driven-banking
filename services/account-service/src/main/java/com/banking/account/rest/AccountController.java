package com.banking.account.rest;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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
