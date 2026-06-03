package com.banking.account;

import com.banking.account.domain.Account;
import com.banking.account.domain.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AccountServiceApplicationTests {

    static DockerImageName postgresImage = DockerImageName.parse("postgres:16-alpine");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(postgresImage)
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafka::getBootstrapServers);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void shouldGetAllAccounts() {
        var account = new Account();
        account.setCustomerId("cust-1");
        account.setAccountNumber("1234567890");
        account.setAccountType(Account.AccountType.CHECKING);
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency("USD");
        account.setActive(true);
        accountRepository.save(account);

        var response = restTemplate.getForEntity("/api/v1/accounts", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(accountRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetAccountById() {
        var account = new Account();
        account.setCustomerId("cust-2");
        account.setAccountNumber("0987654321");
        account.setAccountType(Account.AccountType.SAVINGS);
        account.setBalance(new BigDecimal("500.00"));
        account.setCurrency("USD");
        account.setActive(true);
        var saved = accountRepository.save(account);

        var response = restTemplate.getForEntity("/api/v1/accounts/" + saved.getId(), Account.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccountNumber()).isEqualTo("0987654321");
    }

    @Test
    void shouldReturn404ForNonExistentAccount() {
        var response = restTemplate.getForEntity("/api/v1/accounts/non-existent", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDepositFunds() {
        var account = new Account();
        account.setCustomerId("cust-3");
        account.setAccountNumber("5555555555");
        account.setAccountType(Account.AccountType.CHECKING);
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency("USD");
        account.setActive(true);
        var saved = accountRepository.save(account);

        var depositResponse = restTemplate.postForEntity(
            "/api/v1/accounts/" + saved.getId() + "/deposit",
            Map.of("amount", 250.00),
            Account.class);

        assertThat(depositResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(depositResponse.getBody()).isNotNull();
        assertThat(depositResponse.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void shouldGetAccountsByCustomer() {
        var account1 = new Account();
        account1.setCustomerId("cust-group");
        account1.setAccountNumber("1111111111");
        account1.setAccountType(Account.AccountType.CHECKING);
        account1.setBalance(BigDecimal.ZERO);
        account1.setCurrency("USD");
        account1.setActive(true);
        accountRepository.save(account1);

        var account2 = new Account();
        account2.setCustomerId("cust-group");
        account2.setAccountNumber("2222222222");
        account2.setAccountType(Account.AccountType.SAVINGS);
        account2.setBalance(new BigDecimal("1000.00"));
        account2.setCurrency("USD");
        account2.setActive(true);
        accountRepository.save(account2);

        var response = restTemplate.getForEntity("/api/v1/accounts/customer/cust-group", Account[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }
}
