package com.kte.backend.transaction.internal.services.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.transaction.Transaction;
import com.kte.backend.transaction.TransactionStatus;
import com.kte.backend.transaction.TransactionType;
import com.kte.backend.transaction.internal.dto.response.TransactionResponse;
import com.kte.backend.transaction.internal.factory.TransactionsFactory;
import com.kte.backend.transaction.internal.mapper.TransactionMapper;
import com.kte.backend.transaction.internal.repository.TransactionRepository;
import com.kte.backend.transaction.internal.validator.TransactionValidator;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
}
