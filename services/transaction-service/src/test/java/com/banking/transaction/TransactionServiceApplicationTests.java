package com.banking.transaction;

import com.banking.transaction.domain.Transaction;
import com.banking.transaction.domain.TransactionRepository;
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
class TransactionServiceApplicationTests {

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
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    void shouldCreateTransfer() {
        var request = Map.of(
            "idempotencyKey", "txn-001",
            "sourceAccountId", "source-123",
            "targetAccountId", "target-456",
            "amount", 500.00,
            "currency", "USD",
            "description", "Test transfer"
        );

        var response = restTemplate.postForEntity("/api/v1/transactions/transfer", request, Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(Transaction.TransactionStatus.PENDING);
        assertThat(response.getBody().getType()).isEqualTo(Transaction.TransactionType.TRANSFER);
        assertThat(response.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldCreateMultipleTransfers() {
        var txn1 = Map.of(
            "idempotencyKey", "txn-002",
            "sourceAccountId", "src-1", "targetAccountId", "tgt-1",
            "amount", 100.00, "currency", "USD", "description", "First");
        var txn2 = Map.of(
            "idempotencyKey", "txn-003",
            "sourceAccountId", "src-2", "targetAccountId", "tgt-2",
            "amount", 200.00, "currency", "EUR", "description", "Second");

        restTemplate.postForEntity("/api/v1/transactions/transfer", txn1, Transaction.class);
        restTemplate.postForEntity("/api/v1/transactions/transfer", txn2, Transaction.class);

        assertThat(transactionRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldGetTransactionById() {
        var request = Map.of(
            "idempotencyKey", "txn-004",
            "sourceAccountId", "src-get",
            "targetAccountId", "tgt-get",
            "amount", 1000.00,
            "currency", "USD",
            "description", "Get test"
        );
        var created = restTemplate.postForEntity("/api/v1/transactions/transfer", request, Transaction.class);
        var txnId = created.getBody().getId();

        var response = restTemplate.getForEntity("/api/v1/transactions/" + txnId, Transaction.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(txnId);
        assertThat(response.getBody().getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void shouldReturn404ForNonExistentTransaction() {
        var response = restTemplate.getForEntity("/api/v1/transactions/bad-id", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
