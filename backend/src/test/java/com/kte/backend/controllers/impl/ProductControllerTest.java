package com.kte.backend.controllers.impl;

import com.kte.backend.config.SecurityConfig;
import com.kte.backend.mapper.ProductMapper;
import com.kte.backend.models.dto.request.ProductRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.models.dto.response.ProductResponse;
import com.kte.backend.models.entity.Category;
import com.kte.backend.models.entity.Product;
import com.kte.backend.security.JwtTokenService;
import com.kte.backend.services.catalog.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("ProductController Test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private ProductMapper productMapper;

    private ObjectMapper objectMapper;


    private Product product;
    private Category category;
    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private ProductResponse updatedProductResponse;


    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        category = Category.builder()
                .id("1")
                .name("Electronics")
                .build();

        product = Product.builder()
                .id("1")
                .name("Compüter")
                .sku("COMP123")
                .price(new BigDecimal("999.99"))
                .category(category)
                .description("A high-end computer")
                .imageUrl(null)
                .stockQuantity(10)
                .build();

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
        mockMvc.perform(multipart("/api/v1/products")
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
    void updateProduct() {
    }

    @Test
    void getAllProducts() {
    }

    @Test
    void getProductById() {
    }

    @Test
    void deleteProduct() {
    }
}