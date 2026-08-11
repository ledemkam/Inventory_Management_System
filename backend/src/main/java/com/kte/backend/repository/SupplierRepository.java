package com.kte.backend.repository;

import com.kte.backend.models.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, String> {
    Optional<Supplier> findByNameIgnoreCase(String name);

}
