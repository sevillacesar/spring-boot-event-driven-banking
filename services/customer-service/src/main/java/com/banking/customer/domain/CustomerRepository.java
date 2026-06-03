package com.banking.customer.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByDocumentNumber(String documentNumber);
    boolean existsByEmail(String email);
    boolean existsByDocumentNumber(String documentNumber);
}
