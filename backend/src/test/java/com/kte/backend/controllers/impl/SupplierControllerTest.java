package com.kte.backend.controllers.impl;

import com.kte.backend.config.SecurityConfig;
import com.kte.backend.mapper.SupplierMapper;
import com.kte.backend.models.dto.request.SupplierRequest;
import com.kte.backend.models.dto.response.SupplierResponse;
import com.kte.backend.models.entity.Supplier;
import com.kte.backend.security.JwtTokenService;
import com.kte.backend.services.catalog.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(SupplierController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("SupplierController Test")
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupplierService supplierService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private SupplierMapper supplierMapper;

    private ObjectMapper objectMapper;

    private Supplier supplier;
    private SupplierResponse supplierResponse;
    private SupplierRequest supplierRequest;
    private SupplierResponse updatedSupplierResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        String supplierId = "1";

        supplier = Supplier.builder()
                .id(supplierId)
                .name("Supplier_A")
                .build();
        supplierResponse = SupplierResponse.builder()
                .id(supplierId)
                .name("Supplier_A")
                .build();
        supplierRequest = SupplierRequest.builder()
                .name("Supplier_A")
                .build();
    }

    @Test
    @DisplayName("should return created supplier")
    @WithMockUser(roles = "MANAGER")
    void should_Return_created_Supplier() throws Exception {
    }
}