package com.vaistra.repository;


import com.vaistra.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByproductNameIgnoreCase(String productName);
    Optional<Product> findByProductName(String productName);
}
