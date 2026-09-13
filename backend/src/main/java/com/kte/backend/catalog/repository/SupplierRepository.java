package com.kte.backend.catalog.repository;

import com.kte.backend.catalog.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, String> {
    Optional<Supplier> findByNameIgnoreCase(String name);

}
