package com.kte.backend.catalog.internal.services.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.catalog.internal.mapper.ProductMapper;
import com.kte.backend.catalog.internal.dto.request.ProductRequest;
import com.kte.backend.catalog.dto.response.CategoryResponse;
import com.kte.backend.catalog.dto.response.ProductResponse;
import com.kte.backend.catalog.Category;
import com.kte.backend.catalog.Product;
import com.kte.backend.catalog.repository.ProductRepository;
import com.kte.backend.catalog.internal.services.CategoryService;
import com.kte.backend.catalog.internal.storage.FileStorageService;
import com.kte.backend.catalog.validator.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
    @DisplayName("Should throw exception when creating product with existing SKU")
    void should_Throw_Exception_When_Creating_Product_With_Existing_SKU() {
        // Given
        doThrow(new IllegalArgumentException("Product with SKU already exists")).when(productValidator).checkProductAlreadyExistsBySku(productRequest.sku());

        // When / Then
        assertThatThrownBy(() -> productService.create(productRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product with SKU already exists");

        verify(productValidator).checkProductAlreadyExistsBySku(productRequest.sku());
        verifyNoInteractions(productRepository);
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

    @Test
    @DisplayName("Should find product by id when it exists")
    void should_Find_Product_By_Id_When_it_exists() {
        // Given
        when(productValidator.findProductOrThrow(product.getId())).thenReturn(product);
        when(productMapper.entityToDto(product)).thenReturn(productResponse);

        // When
        ProductResponse response = productService.findById(product.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(product.getName());

        verify(productValidator).findProductOrThrow(product.getId());
        verify(productMapper).entityToDto(product);
    }

    @Test
    @DisplayName("Should find all products with pagination")
    void should_find_All_Products() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.entityToDto(product)).thenReturn(productResponse);

        // When
        PageResponse<ProductResponse> responses = productService.findAll(pageable);

        // Then
        assertThat(responses).isNotNull();

        verify(productRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should delete product when it exists")
    void should_Delete_Product_When_it_exists() {
        // Given
        when(productValidator.findProductOrThrow(product.getId())).thenReturn(product);
        doNothing().when(productRepository).delete(product);

        // When
        productService.delete(product.getId());

        // Then
        verify(productValidator).findProductOrThrow(product.getId());
        verify(productRepository).delete(product);
    }

}
