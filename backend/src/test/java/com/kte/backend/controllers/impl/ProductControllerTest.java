package com.kte.backend.controllers.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kte.backend.common.PageResponse;
import com.kte.backend.config.SecurityConfig;
import com.kte.backend.mapper.ProductMapper;
import com.kte.backend.models.dto.request.ProductRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.models.dto.response.ProductResponse;
import com.kte.backend.security.JwtTokenService;
import com.kte.backend.services.catalog.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
@Import(SecurityConfig.class)
@DisplayName("ProductController Test")
class ProductControllerTest {

    // RestTestClient does not support multipart requests when bound to MockMvc
    // (spring-projects/spring-framework#35569): the client serialises the parts but
    // nothing parses them server-side. The multipart create/update endpoints therefore
    // still use MockMvc, while the plain JSON endpoints use RestTestClient.
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private ProductMapper productMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private ProductResponse updatedProductResponse;


    @BeforeEach
    void setUp() {
        productRequest = ProductRequest.builder()
                .name("Compüter")
                .sku("COMP123")
                .price(new BigDecimal("999.99"))
                .categoryId("1")
                .description("A high-end computer")
                .image(null)
                .stockQuantity(10)
                .build();

        CategoryResponse c = CategoryResponse.builder()
                .id("1")
                .name("Electronics")
                .build();

        productResponse = ProductResponse.builder()
                .id("1")
                .name("Compüter")
                .sku("COMP123")
                .price(new BigDecimal("999.99"))
                .category(c)
                .description("A high-end computer")
                .imageUrl(null)
                .stockQuantity(10)
                .build();

        updatedProductResponse = ProductResponse.builder()
                .id("1")
                .name("Fernsehen")
                .sku("TV456")
                .price(new BigDecimal("499.99"))
                .category(c)
                .description("smart TV")
                .imageUrl(null)
                .stockQuantity(12)
                .build();
    }

    @Test
    @DisplayName("should return created product")
    @WithMockUser(roles = "MANAGER")
    void should_Return_created_Product() throws Exception {
        //Given
        when(productService.create(productRequest)).thenReturn(productResponse);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");

        MockMultipartFile productPart = new MockMultipartFile(
                "product",
                "product",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(productRequest));

        //When & Then
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/products")
                        .file(productPart)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Compüter"))
                .andExpect(jsonPath("$.sku").value("COMP123"))
                .andExpect(jsonPath("$.price").value(999.99))
                .andExpect(jsonPath("$.category.id").value("1"))
                .andExpect(jsonPath("$.category.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("A high-end computer"))
                .andExpect(jsonPath("$.imageUrl").doesNotExist())
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    @Test
    @DisplayName("should return updated product")
    @WithMockUser(roles = "MANAGER")
    void should_Return_updating_Product() throws Exception {
        //Given
        when(productService.update("1", productRequest)).thenReturn(updatedProductResponse);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");

        MockMultipartFile productPart = new MockMultipartFile(
                "product",
                "product",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(productRequest));

        //When & Then
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/products/{product-id}", "1")
                        .file(productPart)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Fernsehen"))
                .andExpect(jsonPath("$.sku").value("TV456"))
                .andExpect(jsonPath("$.price").value(499.99))
                .andExpect(jsonPath("$.category.id").value("1"))
                .andExpect(jsonPath("$.category.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("smart TV"))
                .andExpect(jsonPath("$.imageUrl").doesNotExist())
                .andExpect(jsonPath("$.stockQuantity").value(12));
    }

    @Test
    @DisplayName("should return all products")
    void should_Return_all_Products() {
        //Given
        final PageResponse<ProductResponse> ProductResponsePageResponse = PageResponse.<ProductResponse>builder()
                .content(List.of(productResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();
        when(productService.findAll(any())).thenReturn(ProductResponsePageResponse);
        //WHEN & THEN
        restTestClient.get().uri("/api/v1/products")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo("1")
                .jsonPath("$.content[0].name").isEqualTo("Compüter");
    }

    @Test
    @DisplayName("should return product by id")
    void should_Return_Product_By_Id() {
        String productId = "1";
        when(productService.findById(productId)).thenReturn(productResponse);
        //WHEN & THEN
        restTestClient.get().uri("/api/v1/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1");
    }

    @Test
    @DisplayName("should delete product")
    @WithMockUser(roles = "MANAGER")
    void should_Delete_Product() {
        String productId = "1";
        doNothing().when(productService).delete(productId);
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");
        //WHEN & THEN
        restTestClient.delete().uri("/api/v1/products/" + productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isNoContent();
    }
}
