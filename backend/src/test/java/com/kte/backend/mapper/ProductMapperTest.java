package com.kte.backend.mapper;

import com.kte.backend.models.dto.request.ProductRequest;
import com.kte.backend.models.dto.response.ProductResponse;
import com.kte.backend.models.entity.Category;
import com.kte.backend.models.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("ProductMapper Test")
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    @DisplayName("Test entity to DTO mapping")
    void entity_To_Dto() {
        //Given
        Category category = Category.builder()
                .id("1L")
                .name("Electronics")
                .build();

        Product product = Product.builder()
                .id("1L")
                .name("Laptop")
                .sku("SKU-001")
                .price(BigDecimal.valueOf(1500))
                .stockQuantity(10)
                .description("Gaming laptop")
                .imageUrl("http://example.com/laptop.png")
                .category(category)
                .build();

        //When
        ProductResponse dto = productMapper.entityToDto(product);

        //Then
        assertNotNull(dto);

    }

    @Test
    @DisplayName("Test DTO to entity mapping")
    void dto_To_Entity() {
        //Given
        ProductRequest productRequest = ProductRequest.builder()
                .name("Laptop")
                .sku("SKU-001")
                .price(BigDecimal.valueOf(1500))
                .stockQuantity(10)
                .description("Gaming laptop")
                .imageUrl("http://example.com/laptop.png")
                .categoryId("1L")
                .build();

        //When
        Product entity = productMapper.dtoToEntity(productRequest);

        //Then
        assertNotNull(entity);

    }

    @Test
    @DisplayName("Test list of entities to list of DTOs mapping")
    void to_Dto_List() {
        //Given
        Product product1 = Product.builder()
                .name("Laptop")
                .sku("SKU-001")
                .price(BigDecimal.valueOf(1500))
                .stockQuantity(10)
                .build();

        Product product2 = Product.builder()
                .name("Mouse")
                .sku("SKU-002")
                .price(BigDecimal.valueOf(20))
                .stockQuantity(100)
                .build();

        //When
        List<ProductResponse> dtoList = productMapper.toDtoList(List.of(product1, product2));

        //Then
        assertNotNull(dtoList);

    }

    @Test
    @DisplayName("Test updating entity from DTO")
    void update_Entity_From_Dto() {
        //Given
        Product product = Product.builder()
                .id("1L")
                .name("Laptop")
                .sku("SKU-001")
                .price(BigDecimal.valueOf(1500))
                .stockQuantity(10)
                .build();

        ProductRequest productRequest = ProductRequest.builder()
                .name("Laptop Pro")
                .sku("SKU-001-PRO")
                .price(BigDecimal.valueOf(2000))
                .stockQuantity(5)
                .build();

        //When
        productMapper.updateEntityFromDto(productRequest, product);

        //Then
        assertEquals("Laptop Pro", product.getName());
        assertEquals("SKU-001-PRO", product.getSku());
        assertEquals(BigDecimal.valueOf(2000), product.getPrice());
        assertEquals(5, product.getStockQuantity());
        assertEquals("1L", product.getId());
    }
}