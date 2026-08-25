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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .address("123 Main St")
                .build();
        supplierResponse = SupplierResponse.builder()
                .id(supplierId)
                .name("Supplier_A")
                .address("123 Main St")
                .build();
        supplierRequest = SupplierRequest.builder()
                .name("Supplier_A")
                .address("123 Main St")
                .build();
    }

    @Test
    @DisplayName("should return created supplier")
    @WithMockUser(roles = "MANAGER")
    void should_Return_created_Supplier() throws Exception {
        when(supplierService.create(supplierRequest)).thenReturn(supplierResponse);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");
        //WHEN & THEN
        mockMvc.perform(post("/api/v1/suppliers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("Supplier_A")))
                .andExpect(jsonPath("$.address", is("123 Main St")));

    }
}