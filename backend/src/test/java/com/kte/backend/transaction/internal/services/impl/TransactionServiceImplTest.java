package com.kte.backend.transaction.internal.services.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.exception.NameValueRequiredException;
import com.kte.backend.transaction.Transaction;
import com.kte.backend.transaction.TransactionStatus;
import com.kte.backend.transaction.TransactionType;
import com.kte.backend.transaction.internal.dto.request.TransactionRequest;
import com.kte.backend.transaction.internal.dto.response.TransactionResponse;
import com.kte.backend.transaction.internal.factory.TransactionsFactory;
import com.kte.backend.transaction.internal.mapper.TransactionMapper;
import com.kte.backend.transaction.internal.repository.TransactionRepository;
import com.kte.backend.transaction.internal.validator.TransactionValidator;
import com.kte.backend.catalog.Product;
import com.kte.backend.catalog.Supplier;
import com.kte.backend.catalog.validator.ProductValidator;
import com.kte.backend.user.repository.UserRepository;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
class TransactionServiceImplTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private ProductValidator productValidator;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionsFactory transactionsFactory;

    @Mock
    private TransactionValidator transactionValidator;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    @DisplayName("Should return transactions for an existing user")
    void should_Return_Transactions_For_User() {
        // Given
        final String userId = "1";
        final Pageable pageable = PageRequest.of(0, 10);

        final Transaction transaction = Transaction.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(100))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .description("Sold products")
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(100))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .description("Sold products")
                .build();

        final Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(transactionRepository.findAllByUser_Id(userId, pageable)).thenReturn(transactionPage);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final PageResponse<TransactionResponse> result = transactionService.findAllByUserId(userId, pageable);

        // Then
        assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("page", 0)
                .hasFieldOrPropertyWithValue("size", 10)
                .hasFieldOrPropertyWithValue("totalElements", 1);

        assertThat(result.getContent())
                .isNotNull()
                .hasSize(1)
                .containsExactly(expectedResponse);

        verify(userRepository).existsById(userId);
        verify(transactionRepository).findAllByUser_Id(userId, pageable);
    }

    @Test
    @DisplayName("Should throw when fetching transactions for a non-existing user")
    void should_Throw_When_Fetching_Transactions_For_No_Existing_User() {
        // Given
        final String userId = "unknown";
        final Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.existsById(userId)).thenReturn(false);

        // When / Then
        assertThrows(EntityNotFoundException.class,
                () -> transactionService.findAllByUserId(userId, pageable));

        verify(userRepository).existsById(userId);
        verify(transactionRepository, never()).findAllByUser_Id(anyString(), any(Pageable.class));
    }


    @Test
    @DisplayName("Should record a pending purchase when restocking inventory")
    void should_Restock_Inventory_Successfully() {
        // Given
        final TransactionRequest request = TransactionRequest.builder()
                .productId("p1")
                .quantity(5)
                .supplierId("s1")
                .description("Restock")
                .build();

        final Product product = Product.builder().id("p1").name("Laptop")
                .price(BigDecimal.valueOf(500)).stockQuantity(10).build();
        final Supplier supplier = Supplier.builder().id("s1").name("Supplier Inc").build();

        final Transaction transaction = Transaction.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(2500))
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.PENDING)
                .description("Restock")
                .product(product)
                .supplier(supplier)
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(2500))
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.PENDING)
                .description("Restock")
                .build();

        when(productValidator.findProductOrThrow(request.productId())).thenReturn(product);
        when(transactionValidator.requireSupplier(request)).thenReturn(supplier);
        when(transactionsFactory.buildTransaction(request, product, supplier, request.quantity(), TransactionType.PURCHASE))
                .thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final TransactionResponse result = transactionService.restockInventory(request);

        // Then
        assertThat(result).isNotNull().isEqualTo(expectedResponse);

        verify(productValidator).findProductOrThrow(request.productId());
        verify(transactionValidator).requireSupplier(request);
        verify(transactionsFactory).buildTransaction(request, product, supplier, request.quantity(), TransactionType.PURCHASE);
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Should throw when restocking without a supplier")
    void should_Throw_When_Restocking_Without_Supplier() {
        // Given
        final TransactionRequest request = TransactionRequest.builder()
                .productId("p1")
                .quantity(5)
                .build();

        final Product product = Product.builder().id("p1").name("Laptop")
                .price(BigDecimal.valueOf(500)).stockQuantity(10).build();

        when(productValidator.findProductOrThrow(request.productId())).thenReturn(product);
        when(transactionValidator.requireSupplier(request))
                .thenThrow(new NameValueRequiredException("Supplier id is required for this operation"));

        // When / Then
        assertThrows(NameValueRequiredException.class, () -> transactionService.restockInventory(request));

        verify(transactionsFactory, never()).buildTransaction(any(), any(), any(), anyInt(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should record a pending sale without a supplier")
    void should_Sell_Successfully_Without_Supplier() {
        // Given
        final TransactionRequest request = TransactionRequest.builder()
                .productId("p1")
                .quantity(3)
                .description("Sold to walk-in customer")
                .build();

        final Product product = Product.builder().id("p1").name("Laptop")
                .price(BigDecimal.valueOf(500)).stockQuantity(10).build();

        final Transaction transaction = Transaction.builder()
                .id("t2")
                .totalProducts(3)
                .totalPrice(BigDecimal.valueOf(1500))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.PENDING)
                .description(request.description())
                .product(product)
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t2")
                .totalProducts(3)
                .totalPrice(BigDecimal.valueOf(1500))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.PENDING)
                .description(request.description())
                .build();

        when(productValidator.findProductOrThrow(request.productId())).thenReturn(product);
        when(transactionValidator.resolveOptionalSupplier(request)).thenReturn(null);
        when(transactionsFactory.buildTransaction(request, product, null, request.quantity(), TransactionType.SALE))
                .thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final TransactionResponse result = transactionService.sell(request);

        // Then
        assertThat(result).isNotNull().isEqualTo(expectedResponse);

        verify(transactionValidator).resolveOptionalSupplier(request);
        verify(transactionsFactory).buildTransaction(request, product, null, request.quantity(), TransactionType.SALE);
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Should record a pending return to supplier")
    void should_Return_To_Supplier_Successfully() {
        // Given
        final TransactionRequest request = TransactionRequest.builder()
                .productId("p1")
                .quantity(2)
                .supplierId("s1")
                .description("Defective units")
                .build();

        final Product product = Product.builder().id("p1").name("Laptop")
                .price(BigDecimal.valueOf(500)).stockQuantity(10).build();
        final Supplier supplier = Supplier.builder().id("s1").name("Supplier Inc").build();

        final Transaction transaction = Transaction.builder()
                .id("t3")
                .totalProducts(2)
                .totalPrice(BigDecimal.valueOf(1000))
                .transactionType(TransactionType.RETURN_TO_SUPPLIER)
                .status(TransactionStatus.PENDING)
                .description("Defective units")
                .product(product)
                .supplier(supplier)
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t3")
                .totalProducts(2)
                .totalPrice(BigDecimal.valueOf(1000))
                .transactionType(TransactionType.RETURN_TO_SUPPLIER)
                .status(TransactionStatus.PENDING)
                .description("Defective units")
                .build();

        when(productValidator.findProductOrThrow(request.productId())).thenReturn(product);
        when(transactionValidator.requireSupplier(request)).thenReturn(supplier);
        when(transactionsFactory.buildTransaction(request, product, supplier, request.quantity(), TransactionType.RETURN_TO_SUPPLIER))
                .thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final TransactionResponse result = transactionService.returnToSupplier(request);

        // Then
        assertThat(result).isNotNull().isEqualTo(expectedResponse);

        verify(transactionValidator).requireSupplier(request);
        verify(transactionsFactory).buildTransaction(request, product, supplier, request.quantity(), TransactionType.RETURN_TO_SUPPLIER);
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Should throw when the target status is null")
    void should_Throw_When_Target_Status_Is_Null() {
        // When / Then
        assertThrows(NameValueRequiredException.class,
                () -> transactionService.updateTransactionStatus("t1", null));

        verifyNoInteractions(transactionValidator, transactionsFactory, transactionRepository);
    }

    @Test
    @DisplayName("Should return current state without changes when status is unchanged")
    void should_Return_Same_Dto_When_Status_Unchanged() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id("t1")
                .status(TransactionStatus.PENDING)
                .transactionType(TransactionType.SALE)
                .totalProducts(2)
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .status(TransactionStatus.PENDING)
                .transactionType(TransactionType.SALE)
                .totalProducts(2)
                .build();

        when(transactionValidator.findTransactionOrThrow("t1")).thenReturn(transaction);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final TransactionResponse result = transactionService.updateTransactionStatus("t1", TransactionStatus.PENDING);

        // Then
        assertThat(result).isEqualTo(expectedResponse);

        verifyNoInteractions(transactionsFactory);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when trying to change the status of a canceled transaction")
    void should_Throw_When_Transaction_Already_Canceled() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id("t1")
                .status(TransactionStatus.CANCELED)
                .transactionType(TransactionType.SALE)
                .totalProducts(2)
                .build();

        when(transactionValidator.findTransactionOrThrow("t1")).thenReturn(transaction);

        // When / Then
        assertThrows(NameValueRequiredException.class,
                () -> transactionService.updateTransactionStatus("t1", TransactionStatus.PENDING));

        verifyNoInteractions(transactionsFactory);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should apply stock movement when completing a transaction")
    void should_Apply_Stock_Movement_When_Completing_Transaction() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id("t1")
                .status(TransactionStatus.PENDING)
                .transactionType(TransactionType.PURCHASE)
                .totalProducts(5)
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .status(TransactionStatus.COMPLETED)
                .transactionType(TransactionType.PURCHASE)
                .totalProducts(5)
                .build();

        when(transactionValidator.findTransactionOrThrow("t1")).thenReturn(transaction);
        doNothing().when(transactionsFactory).applyStockMovement(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final TransactionResponse result = transactionService.updateTransactionStatus("t1", TransactionStatus.COMPLETED);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);

        verify(transactionsFactory).applyStockMovement(transaction);
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Should reverse stock movement when canceling a completed transaction")
    void should_Reverse_Stock_Movement_When_Canceling_Completed_Transaction() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id("t1")
                .status(TransactionStatus.COMPLETED)
                .transactionType(TransactionType.SALE)
                .totalProducts(5)
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .status(TransactionStatus.CANCELED)
                .transactionType(TransactionType.SALE)
                .totalProducts(5)
                .build();

        when(transactionValidator.findTransactionOrThrow("t1")).thenReturn(transaction);
        doNothing().when(transactionsFactory).reverseStockMovement(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final TransactionResponse result = transactionService.updateTransactionStatus("t1", TransactionStatus.CANCELED);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.CANCELED);

        verify(transactionsFactory).reverseStockMovement(transaction);
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Should not reverse stock movement when canceling a non-completed transaction")
    void should_Not_Reverse_Stock_Movement_When_Canceling_Non_Completed_Transaction() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id("t1")
                .status(TransactionStatus.PENDING)
                .transactionType(TransactionType.SALE)
                .totalProducts(5)
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .status(TransactionStatus.CANCELED)
                .transactionType(TransactionType.SALE)
                .totalProducts(5)
                .build();

        when(transactionValidator.findTransactionOrThrow("t1")).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final TransactionResponse result = transactionService.updateTransactionStatus("t1", TransactionStatus.CANCELED);

        // Then
        assertThat(result).isEqualTo(expectedResponse);

        verify(transactionsFactory, never()).reverseStockMovement(any());
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Should throw when trying to reopen a completed transaction as pending")
    void should_Throw_When_Reopening_Completed_Transaction() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id("t1")
                .status(TransactionStatus.COMPLETED)
                .transactionType(TransactionType.SALE)
                .totalProducts(5)
                .build();

        when(transactionValidator.findTransactionOrThrow("t1")).thenReturn(transaction);

        // When / Then
        assertThrows(NameValueRequiredException.class,
                () -> transactionService.updateTransactionStatus("t1", TransactionStatus.PENDING));

        verifyNoInteractions(transactionsFactory);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when trying to reopen a completed transaction as processing")
    void should_Throw_When_Reopening_Completed_Transaction_As_Processing() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id("t1")
                .status(TransactionStatus.COMPLETED)
                .transactionType(TransactionType.SALE)
                .totalProducts(5)
                .build();

        when(transactionValidator.findTransactionOrThrow("t1")).thenReturn(transaction);

        // When / Then
        assertThrows(NameValueRequiredException.class,
                () -> transactionService.updateTransactionStatus("t1", TransactionStatus.PROCESSING));

        verifyNoInteractions(transactionsFactory);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow moving between pending and processing when not completed")
    void should_Allow_Transition_Between_Non_Completed_Statuses() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id("t1")
                .status(TransactionStatus.PENDING)
                .transactionType(TransactionType.SALE)
                .totalProducts(5)
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .status(TransactionStatus.PROCESSING)
                .transactionType(TransactionType.SALE)
                .totalProducts(5)
                .build();

        when(transactionValidator.findTransactionOrThrow("t1")).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final TransactionResponse result = transactionService.updateTransactionStatus("t1", TransactionStatus.PROCESSING);

        // Then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PROCESSING);

        verifyNoInteractions(transactionsFactory);
        verify(transactionRepository).save(transaction);
    }

    @Test
    @DisplayName("Should throw when trying to create a transaction directly")
    void should_Throw_When_Creating_Transaction_Directly() {
        // Given
        final TransactionRequest request = TransactionRequest.builder()
                .productId("p1")
                .quantity(1)
                .build();

        // When / Then
        assertThrows(UnsupportedOperationException.class, () -> transactionService.create(request));

        verifyNoInteractions(transactionRepository, transactionMapper);
    }

    @Test
    @DisplayName("Should throw when trying to update a transaction directly")
    void should_Throw_When_Updating_Transaction_Directly() {
        // Given
        final TransactionRequest request = TransactionRequest.builder()
                .productId("p1")
                .quantity(1)
                .build();

        // When / Then
        assertThrows(UnsupportedOperationException.class, () -> transactionService.update("t1", request));

        verifyNoInteractions(transactionRepository, transactionMapper);
    }

    @Test
    @DisplayName("Should return all transactions with pagination")
    void should_Find_All_Transactions() {
        // Given
        final Pageable pageable = PageRequest.of(0, 10);

        final Transaction transaction = Transaction.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(100))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .description("Sold products")
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(100))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .description("Sold products")
                .build();

        final Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

        when(transactionRepository.findAll(pageable)).thenReturn(transactionPage);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final PageResponse<TransactionResponse> result = transactionService.findAll(pageable);

        // Then
        assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("page", 0)
                .hasFieldOrPropertyWithValue("size", 10)
                .hasFieldOrPropertyWithValue("totalElements", 1);

        assertThat(result.getContent()).containsExactly(expectedResponse);

        verify(transactionRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should return transactions matching the search text")
    void should_Search_Transactions() {
        // Given
        final String searchText = "laptop";
        final Pageable pageable = PageRequest.of(0, 10);

        final Transaction transaction = Transaction.builder()
                .id("t1")
                .totalProducts(2)
                .totalPrice(BigDecimal.valueOf(200))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.PENDING)
                .description("Sold laptop")
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(2)
                .totalPrice(BigDecimal.valueOf(200))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.PENDING)
                .description("Sold laptop")
                .build();

        final Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

        when(transactionRepository.searchTransactions(searchText, pageable)).thenReturn(transactionPage);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final PageResponse<TransactionResponse> result = transactionService.search(searchText, pageable);

        // Then
        assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("totalElements", 1);
        assertThat(result.getContent()).containsExactly(expectedResponse);

        verify(transactionRepository).searchTransactions(searchText, pageable);
    }

    @Test
    @DisplayName("Should return transactions for a given month and year")
    void should_Search_Transactions_By_Month_And_Year() {
        // Given
        final int month = 9;
        final int year = 2026;
        final Pageable pageable = PageRequest.of(0, 10);

        final Transaction transaction = Transaction.builder()
                .id("t1")
                .totalProducts(1)
                .totalPrice(BigDecimal.valueOf(50))
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.PENDING)
                .description("Restock")
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(1)
                .totalPrice(BigDecimal.valueOf(50))
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.PENDING)
                .description("Restock")
                .build();

        final Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

        when(transactionRepository.findAllByMonthAndYear(month, year, pageable)).thenReturn(transactionPage);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final PageResponse<TransactionResponse> result = transactionService.searchByMonthAndYear(month, year, pageable);

        // Then
        assertThat(result).isNotNull()
                .hasFieldOrPropertyWithValue("totalElements", 1);
        assertThat(result.getContent()).containsExactly(expectedResponse);

        verify(transactionRepository).findAllByMonthAndYear(month, year, pageable);
    }

    @Test
    @DisplayName("Should return a transaction by id when it exists")
    void should_Find_Transaction_By_Id() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(100))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .description("Sold products")
                .build();

        final TransactionResponse expectedResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(100))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .description("Sold products")
                .build();

        when(transactionValidator.findTransactionOrThrow("t1")).thenReturn(transaction);
        when(transactionMapper.entityToDto(transaction)).thenReturn(expectedResponse);

        // When
        final TransactionResponse result = transactionService.findById("t1");

        // Then
        assertThat(result).isEqualTo(expectedResponse);

        verify(transactionValidator).findTransactionOrThrow("t1");
    }

    @Test
    @DisplayName("Should throw when the transaction to find does not exist")
    void should_Throw_When_Transaction_To_Find_Does_Not_Exist() {
        // Given
        when(transactionValidator.findTransactionOrThrow("unknown"))
                .thenThrow(new EntityNotFoundException("Transaction not found"));

        // When / Then
        assertThrows(EntityNotFoundException.class, () -> transactionService.findById("unknown"));

        verifyNoInteractions(transactionMapper);
    }

    @Test
    @DisplayName("Should delete a transaction when it exists")
    void should_Delete_Transaction_When_It_Exists() {
        // Given
        final Transaction transaction = Transaction.builder()
                .id("t1")
                .status(TransactionStatus.PENDING)
                .transactionType(TransactionType.SALE)
                .totalProducts(1)
                .build();

        when(transactionValidator.findTransactionOrThrow("t1")).thenReturn(transaction);
        doNothing().when(transactionRepository).delete(transaction);

        // When
        transactionService.delete("t1");

        // Then
        verify(transactionValidator).findTransactionOrThrow("t1");
        verify(transactionRepository).delete(transaction);
    }
}
