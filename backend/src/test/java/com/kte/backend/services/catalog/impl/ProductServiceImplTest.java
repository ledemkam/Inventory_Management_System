package com.kte.backend.services.catalog.impl;

import com.kte.backend.mapper.ProductMapper;
import com.kte.backend.models.dto.request.ProductRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.models.dto.response.ProductResponse;
import com.kte.backend.models.entity.Category;
import com.kte.backend.models.entity.Product;
import com.kte.backend.repository.ProductRepository;
import com.kte.backend.services.catalog.CategoryService;
import com.kte.backend.services.storage.FileStorageService;
import com.kte.backend.validator.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductValidator productValidator;

    @Mock
    private CategoryService categoryService;

    @Mock
    private FileStorageService fileStorageService;

    private Product product;
    private Category category;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Should create product when it does not exist")
    void should_Create_Product_When_no_Exist() {
        // Given
        doNothing().when(productValidator).checkProductAlreadyExistsBySku(productRequest.sku());
        when(categoryService.findCategoryOrThrow(productRequest.categoryId())).thenReturn(category);
        when(productMapper.dtoToEntity(productRequest)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.entityToDto(product)).thenReturn(productResponse);

        // When
        ProductResponse response = productService.create(productRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(productRequest.name());

        verify(productValidator).checkProductAlreadyExistsBySku(productRequest.sku());
        verify(productRepository).save(product);
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("Should update product when it exists")
    void should_Update_Product_When_it_exists() {
        // Given
        when(productValidator.findProductOrThrow(product.getId())).thenReturn(product);
        when(categoryService.findCategoryOrThrow(productRequest.categoryId())).thenReturn(category);
        doNothing().when(productMapper).updateEntityFromDto(productRequest, product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.entityToDto(product)).thenReturn(productResponse);

        // When
        ProductResponse response = productService.update(product.getId(), productRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(productRequest.name());

        verify(productMapper).updateEntityFromDto(productRequest, product);
        verify(productRepository).save(product);
    }

}
