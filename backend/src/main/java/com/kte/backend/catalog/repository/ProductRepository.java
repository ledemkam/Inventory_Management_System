package com.kte.backend.catalog.repository;


import com.kte.backend.catalog.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByNameIgnoreCase(String name);

    Optional<Product> findBySkuIgnoreCase(String sku);

}
