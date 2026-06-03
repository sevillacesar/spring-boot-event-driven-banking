package com.banking.customer;

import com.banking.customer.domain.CustomerRepository;
import com.banking.customer.dto.CreateCustomerRequest;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CustomerServiceApplicationTests {

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
    private CustomerRepository customerRepository;

    @Test
    void shouldCreateCustomer() {
        var request = new CreateCustomerRequest("Juan", "Perez", "juan@test.com", "12345678", "+593999999999");

        var response = restTemplate.postForEntity("/api/v1/customers", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(customerRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetAllCustomers() {
        customerRepository.deleteAll();

        restTemplate.postForEntity("/api/v1/customers",
            new CreateCustomerRequest("A", "B", "a@test.com", "111", "+111"), String.class);
        restTemplate.postForEntity("/api/v1/customers",
            new CreateCustomerRequest("C", "D", "c@test.com", "222", "+222"), String.class);

        var response = restTemplate.getForEntity("/api/v1/customers", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(customerRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldReturn404ForNonExistentCustomer() {
        var response = restTemplate.getForEntity("/api/v1/customers/non-existent-id", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        var request = new CreateCustomerRequest("Juan", "Perez", "duplicate@test.com", "12345678", "+593999999999");
        restTemplate.postForEntity("/api/v1/customers", request, String.class);

        var response = restTemplate.postForEntity("/api/v1/customers", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(customerRepository.count()).isEqualTo(1);
    }
}
