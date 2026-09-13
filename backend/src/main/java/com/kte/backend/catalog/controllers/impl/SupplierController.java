package com.kte.backend.catalog.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.catalog.controllers.UISupplierController;
import com.kte.backend.catalog.dto.request.SupplierRequest;
import com.kte.backend.catalog.dto.response.SupplierResponse;
import com.kte.backend.catalog.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController implements UISupplierController {

    private final SupplierService supplierService;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody final SupplierRequest request) {
        log.info("Received request to create supplier with request: {}", request);
        SupplierResponse createdSupplier = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSupplier);
    }

    @Override
    @PutMapping("/{supplier-id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable("supplier-id")
            @Valid final String id,
            @RequestBody final SupplierRequest supplierRequest) {
        log.info("Received request to update supplier with id: {} and request: {}", id, supplierRequest);
        SupplierResponse updatedSupplier = supplierService.update(id, supplierRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedSupplier);
    }


    @Override
    @GetMapping
    public ResponseEntity<PageResponse<SupplierResponse>> getAllSuppliers(final Pageable pageable) {
        PageResponse<SupplierResponse> suppliers = supplierService.findAll(pageable);
        log.debug("Received request to get all suppliers with pageable: {}", pageable);
        return ResponseEntity.ok(suppliers);
    }

    @Override
    @GetMapping("/{supplier-id}")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @PathVariable("supplier-id") final String id) {
        SupplierResponse supplier = supplierService.findById(id);
        log.debug("Received request to get supplier by id: {}", id);
        return ResponseEntity.ok(supplier);
    }

    @Override
    @DeleteMapping("/{supplier-id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable("supplier-id") final String id) {
        log.info("Received request to delete supplier with id: {}", id);
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
