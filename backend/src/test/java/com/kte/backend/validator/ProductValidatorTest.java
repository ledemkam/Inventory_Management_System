package com.kte.backend.validator;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.models.entity.Product;
import com.kte.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductValidator Unit Tests")
class ProductValidatorTest {

    @InjectMocks
    private ProductValidator productValidator;

    @Mock
    private ProductRepository productRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id("1")
                .name("Laptop")
                .sku("SKU-001")
                .build();
    }

    @Test
    @DisplayName("checkProductAlreadyExistsBySku: does nothing when no product with the sku exists")
    void should_not_throw_when_sku_is_free() {
        //GIVEN
        when(productRepository.findBySkuIgnoreCase("SKU-001")).thenReturn(Optional.empty());

        //WHEN / THEN
        assertThatCode(() -> productValidator.checkProductAlreadyExistsBySku("SKU-001"))
                .doesNotThrowAnyException();
        verify(productRepository).findBySkuIgnoreCase("SKU-001");
    }

    @Test
    @DisplayName("checkProductAlreadyExistsBySku: throws EntityAlreadyExistsException when the sku is taken")
    void should_throw_when_sku_already_exists() {
        //GIVEN
        when(productRepository.findBySkuIgnoreCase("SKU-001")).thenReturn(Optional.of(product));

        //WHEN / THEN
        assertThatThrownBy(() -> productValidator.checkProductAlreadyExistsBySku("SKU-001"))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage("SKU-001");
    }

    @Test
    @DisplayName("findProductOrThrow: returns the product when it exists")
    void should_return_product_when_found() {
        //GIVEN
        when(productRepository.findById("1")).thenReturn(Optional.of(product));

        //WHEN
        Product found = productValidator.findProductOrThrow("1");

        //THEN
        assertThat(found).isSameAs(product);
    }

    @Test
    @DisplayName("findProductOrThrow: throws EntityNotFoundException when the product is missing")
    void should_throw_when_product_not_found() {
        //GIVEN
        when(productRepository.findById("99")).thenReturn(Optional.empty());

        //WHEN / THEN
        assertThatThrownBy(() -> productValidator.findProductOrThrow("99"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found");
    }
}
