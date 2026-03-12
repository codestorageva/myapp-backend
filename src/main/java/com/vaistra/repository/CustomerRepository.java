package com.vaistra.repository;

import com.vaistra.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Integer> {

    boolean existsByEmailIgnoreCase(String email);
    Optional<Customer> findByDisplayName(String displayName);
}
