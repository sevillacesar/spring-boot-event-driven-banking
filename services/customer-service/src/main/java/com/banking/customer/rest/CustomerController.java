package com.banking.customer.rest;

import com.banking.customer.domain.Customer;
import com.banking.customer.domain.CustomerRepository;
import com.banking.customer.dto.CreateCustomerRequest;
import com.banking.customer.dto.CustomerResponse;
import com.banking.customer.event.CustomerEventPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerEventPublisher eventPublisher;

    public CustomerController(CustomerRepository customerRepository, CustomerEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().build();
        }

        var customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setDocumentNumber(request.documentNumber());
        customer.setPhone(request.phone());

        customer = customerRepository.save(customer);
        eventPublisher.publishCustomerCreated(customer);

        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerResponse.from(customer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable String id) {
        return customerRepository.findById(id)
            .map(c -> ResponseEntity.ok(CustomerResponse.from(c)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
            .map(CustomerResponse::from)
            .toList();
    }
}
