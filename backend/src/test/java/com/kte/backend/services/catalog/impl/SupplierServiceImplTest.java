package com.kte.backend.services.catalog.impl;

import com.kte.backend.mapper.SupplierMapper;
import com.kte.backend.models.dto.request.SupplierRequest;
import com.kte.backend.models.dto.response.SupplierResponse;
import com.kte.backend.models.entity.Supplier;
import com.kte.backend.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@DisplayName("SupplierServiceImpl Unit Tests")
class SupplierServiceImplTest {

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier supplier;
    private SupplierRequest supplierRequest;
    private SupplierResponse supplierResponse;

    @BeforeEach
    void setup() {
        supplier = Supplier.builder()
                .id("1")
                .name("Supplier A")
                .build();

        supplierRequest = SupplierRequest.builder()
                .name("Supplier A")
                .build();

        supplierResponse = SupplierResponse.builder()
                .id("1")
                .name("Supplier A")
                .build();
    }


    @Test
    @DisplayName("should create supplier when it does not exist")
    void should_create_supplier_when_exist() {
    }
}