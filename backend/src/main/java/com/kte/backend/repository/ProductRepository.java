package com.kte.backend.repository;


import com.kte.backend.models.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByNameIgnoreCase(String name);

}
