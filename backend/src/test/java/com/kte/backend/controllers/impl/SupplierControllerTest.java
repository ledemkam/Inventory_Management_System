package com.kte.backend.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.config.SecurityConfig;
import com.kte.backend.mapper.SupplierMapper;
import com.kte.backend.models.dto.request.SupplierRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
        updatedSupplierResponse = SupplierResponse.builder()
                .id(supplierId)
                .name("updated_Supplier_A")
                .address("124 Main St")
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

    @Test
    @DisplayName("should return updated supplier")
    @WithMockUser(roles = "MANAGER")
    void should_Return_Update_Supplier() throws Exception {
        String supplierId = "1";
        when(supplierService.update(supplierId, supplierRequest)).thenReturn(updatedSupplierResponse);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");
        //WHEN & THEN
        mockMvc.perform(put("/api/v1/suppliers/" + supplierId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(supplierId)))
                .andExpect(jsonPath("$.name", is("updated_Supplier_A")))
                .andExpect(jsonPath("$.address", is("124 Main St")));
    }

    @Test
    @DisplayName("should return all suppliers")
    void should_Return_All_Suppliers() throws Exception {
        //GIVEN
        final PageResponse<SupplierResponse> supplierResponsePageResponse = PageResponse.<SupplierResponse>builder()
                .content(List.of(supplierResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();
        when(supplierService.findAll(any())).thenReturn(supplierResponsePageResponse);
        //WHEN & THEN
        mockMvc.perform(get("/api/v1/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierResponsePageResponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is("1")))
                .andExpect(jsonPath("$.content[0].name", is("Supplier_A")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return supplier by id")
    void should_Return_Supplier_By_Id() throws Exception {
        String supplierId = "1";
        when(supplierService.findById(supplierId)).thenReturn(supplierResponse);
        //WHEN & THEN
        mockMvc.perform(get("/api/v1/suppliers/" + supplierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierResponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("1")));
    }
}