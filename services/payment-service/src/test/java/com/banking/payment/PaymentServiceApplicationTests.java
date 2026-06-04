package com.banking.payment;

import com.banking.payment.domain.Payment;
import com.banking.payment.domain.PaymentRepository;
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
class PaymentServiceApplicationTests {

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
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void shouldCreatePayment() {
        var request = Map.of(
            "idempotencyKey", "unique-key-001",
            "sourceAccountId", "acc-123",
            "amount", 150.00,
            "currency", "USD",
            "merchant", "Amazon",
            "cardLastFour", "1234",
            "cardholderName", "Juan Perez",
            "description", "Compra online"
        );

        var response = restTemplate.postForEntity("/api/v1/payments", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(paymentRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnExistingPaymentOnDuplicateIdempotencyKey() {
        var request = Map.of(
            "idempotencyKey", "idem-duplicate",
            "sourceAccountId", "acc-111",
            "amount", 50.00,
            "currency", "USD",
            "merchant", "Shopify",
            "cardLastFour", "9876",
            "cardholderName", "Ana Lopez",
            "description", "Duplicate test"
        );

        var first = restTemplate.postForEntity("/api/v1/payments", request, String.class);
        var second = restTemplate.postForEntity("/api/v1/payments", request, String.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(paymentRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldNotReprocessCompletedPayment() {
        var payment = new Payment();
        payment.setIdempotencyKey("idem-process-1");
        payment.setSourceAccountId("acc-456");
        payment.setAmount(new BigDecimal("250.00"));
        payment.setCurrency("USD");
        payment.setMerchant("MercadoLibre");
        payment.setCardLastFour("5678");
        payment.setCardholderName("Maria Garcia");
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setProcessedAt(java.time.LocalDateTime.now());
        var saved = paymentRepository.save(payment);

        var response = restTemplate.postForEntity(
            "/api/v1/payments/" + saved.getId() + "/process", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var updated = paymentRepository.findById(saved.getId());
        assertThat(updated.get().getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
    }

    @Test
    void shouldProcessPayment() {
        var payment = new Payment();
        payment.setIdempotencyKey("idem-process-2");
        payment.setSourceAccountId("acc-456");
        payment.setAmount(new BigDecimal("250.00"));
        payment.setCurrency("USD");
        payment.setMerchant("MercadoLibre");
        payment.setCardLastFour("5678");
        payment.setCardholderName("Maria Garcia");
        payment.setStatus(Payment.PaymentStatus.PENDING);
        var saved = paymentRepository.save(payment);

        var response = restTemplate.postForEntity(
            "/api/v1/payments/" + saved.getId() + "/process", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var updated = paymentRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
    }

    @Test
    void shouldDeclineHighValuePayment() {
        var payment = new Payment();
        payment.setIdempotencyKey("idem-decline-1");
        payment.setSourceAccountId("acc-789");
        payment.setAmount(new BigDecimal("15000.00"));
        payment.setCurrency("USD");
        payment.setMerchant("Tesla");
        payment.setCardLastFour("9999");
        payment.setCardholderName("Carlos Lopez");
        payment.setStatus(Payment.PaymentStatus.PENDING);
        var saved = paymentRepository.save(payment);

        var response = restTemplate.postForEntity(
            "/api/v1/payments/" + saved.getId() + "/process", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var updated = paymentRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(Payment.PaymentStatus.DECLINED);
    }
}
