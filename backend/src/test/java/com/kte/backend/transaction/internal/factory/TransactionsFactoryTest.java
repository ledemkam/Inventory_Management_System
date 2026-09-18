package com.kte.backend.transaction.internal.factory;

import com.kte.backend.catalog.Product;
import com.kte.backend.catalog.Supplier;
import com.kte.backend.catalog.repository.ProductRepository;
import com.kte.backend.exception.NameValueRequiredException;
import com.kte.backend.transaction.Transaction;
import com.kte.backend.transaction.TransactionStatus;
import com.kte.backend.transaction.TransactionType;
import com.kte.backend.transaction.internal.dto.request.TransactionRequest;
import com.kte.backend.user.User;
import com.kte.backend.user.UserRole;
import com.kte.backend.user.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionsFactory Tests")
class TransactionsFactoryTest {

    @Mock
    private UserService userService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private TransactionsFactory transactionsFactory;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id("u1").username("eric").role(UserRole.ADMIN).build();
    }

    @Test
    @DisplayName("Should build a transaction using the provided description")
    void should_Build_Transaction_With_Provided_Description() {
        // Given
        final Product product = Product.builder().id("p1").name("Laptop")
                .price(BigDecimal.valueOf(500)).stockQuantity(10).build();
        final Supplier supplier = Supplier.builder().id("s1").name("Supplier Inc").build();
        final TransactionRequest request = TransactionRequest.builder()
                .productId("p1").quantity(3).supplierId("s1").description("Restock").build();

        when(userService.getCurrentLoggedInUser()).thenReturn(currentUser);

        // When
        final Transaction transaction = transactionsFactory.buildTransaction(
                request, product, supplier, 3, TransactionType.PURCHASE);

        // Then
        assertThat(transaction.getDescription()).isEqualTo("Restock");
        assertThat(transaction.getTotalProducts()).isEqualTo(3);
        assertThat(transaction.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.PURCHASE);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getUser()).isEqualTo(currentUser);
        assertThat(transaction.getProduct()).isEqualTo(product);
        assertThat(transaction.getSupplier()).isEqualTo(supplier);
    }

    @Test
    @DisplayName("Should generate a description when none is provided")
    void should_Build_Transaction_With_Generated_Description_When_Blank() {
        // Given
        final Product product = Product.builder().id("p1").name("Laptop")
                .price(BigDecimal.valueOf(500)).stockQuantity(10).build();
        final TransactionRequest request = TransactionRequest.builder()
                .productId("p1").quantity(2).build();

        when(userService.getCurrentLoggedInUser()).thenReturn(currentUser);

        // When
        final Transaction transaction = transactionsFactory.buildTransaction(
                request, product, null, 2, TransactionType.SALE);

        // Then
        assertThat(transaction.getDescription()).isEqualTo("SALE - 2 x Laptop");
    }

    @Test
    @DisplayName("Should default the unit price to zero when the product has no price")
    void should_Build_Transaction_With_Zero_Price_When_Product_Price_Is_Null() {
        // Given
        final Product product = Product.builder().id("p1").name("Laptop").stockQuantity(10).build();
        final TransactionRequest request = TransactionRequest.builder()
                .productId("p1").quantity(4).build();

        when(userService.getCurrentLoggedInUser()).thenReturn(currentUser);

        // When
        final Transaction transaction = transactionsFactory.buildTransaction(
                request, product, null, 4, TransactionType.SALE);

        // Then
        assertThat(transaction.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should increase stock when applying a purchase")
    void should_Apply_Stock_Movement_For_Purchase() {
        // Given
        final Product product = Product.builder().id("p1").stockQuantity(10).build();
        final Transaction transaction = Transaction.builder()
                .product(product).totalProducts(5).transactionType(TransactionType.PURCHASE).build();
        lenient().when(productRepository.save(product)).thenReturn(product);

        // When
        transactionsFactory.applyStockMovement(transaction);

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(15);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should treat a null stock quantity as zero when applying a purchase")
    void should_Apply_Stock_Movement_With_Null_Stock_As_Zero() {
        // Given
        final Product product = Product.builder().id("p1").stockQuantity(null).build();
        final Transaction transaction = Transaction.builder()
                .product(product).totalProducts(5).transactionType(TransactionType.PURCHASE).build();

        // When
        transactionsFactory.applyStockMovement(transaction);

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should decrease stock when applying a sale with sufficient stock")
    void should_Apply_Stock_Movement_For_Sale_With_Sufficient_Stock() {
        // Given
        final Product product = Product.builder().id("p1").stockQuantity(10).build();
        final Transaction transaction = Transaction.builder()
                .product(product).totalProducts(4).transactionType(TransactionType.SALE).build();

        // When
        transactionsFactory.applyStockMovement(transaction);

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(6);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should decrease stock when applying a return to supplier")
    void should_Apply_Stock_Movement_For_Return_To_Supplier() {
        // Given
        final Product product = Product.builder().id("p1").stockQuantity(10).build();
        final Transaction transaction = Transaction.builder()
                .product(product).totalProducts(2).transactionType(TransactionType.RETURN_TO_SUPPLIER).build();

        // When
        transactionsFactory.applyStockMovement(transaction);

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(8);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should throw and leave stock untouched when applying a sale without enough stock")
    void should_Throw_When_Applying_Sale_With_Insufficient_Stock() {
        // Given
        final Product product = Product.builder().id("p1").name("Laptop").stockQuantity(2).build();
        final Transaction transaction = Transaction.builder()
                .product(product).totalProducts(5).transactionType(TransactionType.SALE).build();

        // When / Then
        assertThatThrownBy(() -> transactionsFactory.applyStockMovement(transaction))
                .isInstanceOf(NameValueRequiredException.class)
                .hasMessageContaining("Insufficient stock for product Laptop: available 2, requested 5");

        assertThat(product.getStockQuantity()).isEqualTo(2);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should decrease stock when reversing a purchase with sufficient stock")
    void should_Reverse_Stock_Movement_For_Purchase_With_Sufficient_Stock() {
        // Given
        final Product product = Product.builder().id("p1").stockQuantity(10).build();
        final Transaction transaction = Transaction.builder()
                .product(product).totalProducts(5).transactionType(TransactionType.PURCHASE).build();

        // When
        transactionsFactory.reverseStockMovement(transaction);

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(5);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should throw when reversing a purchase without enough stock to remove")
    void should_Throw_When_Reversing_Purchase_With_Insufficient_Stock() {
        // Given
        final Product product = Product.builder().id("p1").name("Laptop").stockQuantity(2).build();
        final Transaction transaction = Transaction.builder()
                .product(product).totalProducts(5).transactionType(TransactionType.PURCHASE).build();

        // When / Then
        assertThatThrownBy(() -> transactionsFactory.reverseStockMovement(transaction))
                .isInstanceOf(NameValueRequiredException.class)
                .hasMessageContaining("Insufficient stock for product Laptop: available 2, requested 5");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should increase stock when reversing a sale")
    void should_Reverse_Stock_Movement_For_Sale() {
        // Given
        final Product product = Product.builder().id("p1").stockQuantity(10).build();
        final Transaction transaction = Transaction.builder()
                .product(product).totalProducts(3).transactionType(TransactionType.SALE).build();

        // When
        transactionsFactory.reverseStockMovement(transaction);

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(13);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("Should increase stock when reversing a return to supplier")
    void should_Reverse_Stock_Movement_For_Return_To_Supplier() {
        // Given
        final Product product = Product.builder().id("p1").stockQuantity(10).build();
        final Transaction transaction = Transaction.builder()
                .product(product).totalProducts(1).transactionType(TransactionType.RETURN_TO_SUPPLIER).build();

        // When
        transactionsFactory.reverseStockMovement(transaction);

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(11);
        verify(productRepository).save(product);
    }
}
