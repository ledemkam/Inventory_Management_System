package com.kte.backend.catalog.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.config.SecurityConfig;
import com.kte.backend.catalog.mapper.SupplierMapper;
import com.kte.backend.catalog.dto.request.SupplierRequest;

import com.kte.backend.catalog.dto.response.SupplierResponse;
import com.kte.backend.catalog.Supplier;
import com.kte.backend.user.security.JwtTokenService;
import com.kte.backend.catalog.services.SupplierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(SupplierController.class)
@AutoConfigureRestTestClient
@Import(SecurityConfig.class)
@DisplayName("SupplierController Test")
class SupplierControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private SupplierService supplierService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private SupplierMapper supplierMapper;

    private Supplier supplier;
    private SupplierResponse supplierResponse;
    private SupplierRequest supplierRequest;
    private SupplierResponse updatedSupplierResponse;

    @BeforeEach
    void setUp() {
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
    void should_Return_created_Supplier() {
        when(supplierService.create(supplierRequest)).thenReturn(supplierResponse);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");
        //WHEN & THEN
        restTestClient.post().uri("/api/v1/suppliers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(supplierRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.name").isEqualTo("Supplier_A")
                .jsonPath("$.address").isEqualTo("123 Main St");
    }

    @Test
    @DisplayName("should return updated supplier")
    @WithMockUser(roles = "MANAGER")
    void should_Return_Update_Supplier() {
        String supplierId = "1";
        when(supplierService.update(supplierId, supplierRequest)).thenReturn(updatedSupplierResponse);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");
        //WHEN & THEN
        restTestClient.put().uri("/api/v1/suppliers/" + supplierId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(supplierRequest)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.id").isEqualTo(supplierId)
                .jsonPath("$.name").isEqualTo("updated_Supplier_A")
                .jsonPath("$.address").isEqualTo("124 Main St");
    }

    @Test
    @DisplayName("should return all suppliers")
    void should_Return_All_Suppliers() {
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
        restTestClient.get().uri("/api/v1/suppliers")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo("1")
                .jsonPath("$.content[0].name").isEqualTo("Supplier_A");
    }

    @Test
    @DisplayName("should return supplier by id")
    void should_Return_Supplier_By_Id() {
        String supplierId = "1";
        when(supplierService.findById(supplierId)).thenReturn(supplierResponse);
        //WHEN & THEN
        restTestClient.get().uri("/api/v1/suppliers/" + supplierId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1");
    }

    @Test
    @DisplayName("should delete supplier by id")
    @WithMockUser(roles = "MANAGER")
    void should_Delete_Supplier() {
        String supplierId = "1";
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");
        //WHEN & THEN
        restTestClient.delete().uri("/api/v1/suppliers/" + supplierId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isNoContent();
    }
}
