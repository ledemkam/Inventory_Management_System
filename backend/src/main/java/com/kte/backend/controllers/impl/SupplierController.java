package com.kte.backend.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.controllers.UISupplierController;
import com.kte.backend.models.dto.request.SupplierRequest;
import com.kte.backend.models.dto.response.SupplierResponse;
import com.kte.backend.services.catalog.SupplierService;
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
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody final SupplierRequest request) {
        SupplierResponse createdSupplier = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSupplier);
    }

    @Override
    @PutMapping("/{supplier-id}")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable("supplier-id")
            @Valid final String id,
            @RequestBody final SupplierRequest supplierRequest) {
        SupplierResponse updatedSupplier = supplierService.update(id, supplierRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedSupplier);
    }


    @Override
    public ResponseEntity<PageResponse<SupplierResponse>> getAllSuppliers(Pageable pageable) {
        return null;
    }

    @Override
    public ResponseEntity<SupplierResponse> getSupplierById(String id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteSupplier(String id) {
        return null;
    }
}
