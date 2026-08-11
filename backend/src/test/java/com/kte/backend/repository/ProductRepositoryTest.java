package com.kte.backend.repository;

import com.kte.backend.models.entity.Category;
import com.kte.backend.models.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository Test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    PageRequest pageRequest = PageRequest.of(0, 10);

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        Category Electronic = Category.builder()
                .name("Electronic")
                .build();

        Product phone = Product.builder()
                .name("phone")
                .sku("TESTSKU-PHONE")
                .price(new BigDecimal("10.0"))
                .stockQuantity(100)
                .category(Electronic) // Assuming category is not required for this test
                .imageUrl(null) // Assuming imageUrl is not required for this test
                .description("phone Description")
                .build();

        Product pc = Product.builder()
                .name("pc")
                .sku("TESTSKU-PC")
                .price(new BigDecimal("10.0"))
                .stockQuantity(100)
                .category(Electronic) // Assuming category is not required for this test
                .imageUrl(null) // Assuming imageUrl is not required for this test
                .description("pc Description")
                .build();

        entityManager.persist(Electronic);
        entityManager.persist(phone);
        entityManager.persist(pc);
    }

    @Test
    @DisplayName("Save product with success")
    void save_product_with_success() {
        //Given#
        Product product = Product.builder()
                .name("phone")
                .sku("TESTSKU-PHONE")
                .price(new BigDecimal("10.0"))
                .stockQuantity(100)
                .category(categoryRepository.findByNameIgnoreCase("Electronic").orElse(null)) // Assuming category is not required for this test
                .imageUrl(null) // Assuming imageUrl is not required for this test
                .description("phone Description")
                .build();

        //when
        Product savedProduct = productRepository.save(product);

        //then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
    }

    @Test
    @DisplayName("Check if product exists by name ignoring case")
    void exists_By_Name_IgnoreCase() {

        //when
        Optional<Product> product1 = productRepository.findByNameIgnoreCase("phone");

        //then
        assertThat(product1).isPresent();


    }

    @Test
    @DisplayName("Find all products")
    void find_All_Products() {

        //When
        Page<Product> products = productRepository.findAll(pageRequest);

        //Then
        assertThat(products).isNotEmpty();
    }


    @Test
    @DisplayName("Find product by id")
    void find_product_By_Id() {
        //Given
        Product product = Product.builder()
                .name("phone")
                .sku("TESTSKU-PHONE")
                .price(new BigDecimal("10.0"))
                .stockQuantity(100)
                .category(categoryRepository.findByNameIgnoreCase("Electronic").orElse(null)) // Assuming category is not required for this test
                .imageUrl(null) // Assuming imageUrl is not required for this test
                .description("phone Description")
                .build();

        //When
        Product savedProduct = productRepository.save(product);
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        //Then
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getId()).isEqualTo(savedProduct.getId());
    }


    @Test
    void delete_product_By_Id() {
    }

}