package com.kte.backend.transaction.internal.services.impl;


import com.kte.backend.common.PageResponse;
import com.kte.backend.exception.NameValueRequiredException;
import com.kte.backend.transaction.internal.mapper.TransactionMapper;
import com.kte.backend.transaction.internal.dto.request.TransactionRequest;
import com.kte.backend.transaction.internal.dto.response.TransactionResponse;
import com.kte.backend.catalog.Product;
import com.kte.backend.catalog.Supplier;
import com.kte.backend.transaction.Transaction;
import com.kte.backend.transaction.TransactionStatus;
import com.kte.backend.transaction.TransactionType;
import com.kte.backend.transaction.internal.repository.TransactionRepository;
import com.kte.backend.transaction.internal.services.TransactionService;

import com.kte.backend.transaction.internal.factory.TransactionsFactory;
import com.kte.backend.catalog.validator.ProductValidator;
import com.kte.backend.transaction.internal.validator.TransactionValidator;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.user.UserErrorMessages;
import com.kte.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionMapper transactionMapper;
    private final ProductValidator productValidator;
    private final TransactionRepository transactionRepository;
    private final TransactionsFactory transactionsFactory;
    private final TransactionValidator transactionValidator;
    private final UserRepository userRepository;

    /**
     * Records a pending purchase from a supplier. Stock is increased on completion.
     */
    @Override
    public TransactionResponse restockInventory(TransactionRequest transactionRequest) {
        final int quantity = transactionRequest.quantity();
        final Product product = productValidator.findProductOrThrow(transactionRequest.productId());
        final Supplier supplier = transactionValidator.requireSupplier(transactionRequest);

        final Transaction transaction = transactionsFactory.buildTransaction(
                transactionRequest, product, supplier, quantity, TransactionType.PURCHASE);

        log.info("Recorded PENDING purchase of {} units for product {} from supplier {}",
                quantity, product.getId(), supplier.getId());
        return transactionMapper.entityToDto(transactionRepository.save(transaction));
    }

    /**
     * Records a pending sale to a customer. Stock is decreased on completion.
     */
    @Override
    public TransactionResponse sell(final TransactionRequest transactionRequest) {
        final int quantity = transactionRequest.quantity();
        final Product product = productValidator.findProductOrThrow(transactionRequest.productId());
        final Supplier supplier = transactionValidator.resolveOptionalSupplier(transactionRequest);

        final Transaction transaction = transactionsFactory.buildTransaction(
                transactionRequest, product, supplier, quantity, TransactionType.SALE);

        log.info("Recorded PENDING sale of {} units for product {}", quantity, product.getId());
        return transactionMapper.entityToDto(transactionRepository.save(transaction));
    }

    /**
     * Records a pending return of stock to a supplier. Stock is decreased on completion.
     */
    @Override
    public TransactionResponse returnToSupplier(final TransactionRequest transactionRequest) {
        final int quantity = transactionRequest.quantity();
        final Product product = productValidator.findProductOrThrow(transactionRequest.productId());
        final Supplier supplier = transactionValidator.requireSupplier(transactionRequest);

        final Transaction transaction = transactionsFactory.buildTransaction(
                transactionRequest, product, supplier, quantity, TransactionType.RETURN_TO_SUPPLIER);

        log.info("Recorded PENDING return of {} units for product {} to supplier {}",
                quantity, product.getId(), supplier.getId());
        return transactionMapper.entityToDto(transactionRepository.save(transaction));
    }

    /**
     * Moves a transaction through its lifecycle.
     * <ul>
     *     <li>-&gt; COMPLETED : applies the stock movement for the transaction type.</li>
     *     <li>-&gt; CANCELED  : reverses the stock movement if it had already been completed.</li>
     *     <li>-&gt; PENDING / PROCESSING : allowed only while the transaction is not yet completed.</li>
     * </ul>
     */
    @Override
    public TransactionResponse updateTransactionStatus(final String transactionId,
                                                       final TransactionStatus transactionStatus) {
        if (transactionStatus == null) {
            throw new NameValueRequiredException("Transaction status is required");
        }

        final Transaction transaction = transactionValidator.findTransactionOrThrow(transactionId);
        final TransactionStatus current = transaction.getStatus();

        if (current == transactionStatus) {
            return transactionMapper.entityToDto(transaction);
        }
        if (current == TransactionStatus.CANCELED) {
            throw new NameValueRequiredException("A canceled transaction can no longer change status");
        }

        switch (transactionStatus) {
            case COMPLETED -> transactionsFactory.applyStockMovement(transaction);
            case TransactionStatus status when status == TransactionStatus.CANCELED
                    && current == TransactionStatus.COMPLETED -> transactionsFactory.reverseStockMovement(transaction);
            case TransactionStatus status when (status == TransactionStatus.PENDING || status == TransactionStatus.PROCESSING)
                    && current == TransactionStatus.COMPLETED ->
                    throw new NameValueRequiredException("A completed transaction cannot be reopened");
            case CANCELED, PENDING, PROCESSING -> {
                // No stock movement to make: canceling a not-yet-completed transaction, or
                // moving between PENDING and PROCESSING, never touched stock in the first place.
            }
        }

        transaction.setStatus(transactionStatus);
        log.info("Transaction {} moved from {} to {}", transaction.getId(), current, transactionStatus);
        return transactionMapper.entityToDto(transactionRepository.save(transaction));
    }

//CRUD (from CrudServices)

    @Override
    public TransactionResponse create(TransactionRequest request) {
        throw new UnsupportedOperationException(
                "Use restockInventory, sell or returnToSupplier to create a transaction");
    }

    @Override
    public TransactionResponse update(final String id, final TransactionRequest request) {
        // Only the status of a transaction can change once recorded.
        throw new UnsupportedOperationException("Use updateTransactionStatus to modify a transaction");
    }

    @Override
    public PageResponse<TransactionResponse> findAll(final Pageable pageable) {
        log.debug("Fetching all transactions with pagination: page {}, size {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.of(transactionRepository.findAll(pageable).map(transactionMapper::entityToDto));
    }

    @Override
    public PageResponse<TransactionResponse> search(final String searchText, final Pageable pageable) {
        log.debug("Searching transactions with searchText '{}', pagination: page {}, size {}",
                searchText, pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.of(transactionRepository.searchTransactions(searchText, pageable)
                .map(transactionMapper::entityToDto));
    }

    @Override
    public PageResponse<TransactionResponse> searchByMonthAndYear(final int month, final int year, final Pageable pageable) {
        log.debug("Searching transactions for month {}, year {}, pagination: page {}, size {}",
                month, year, pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.of(transactionRepository.findAllByMonthAndYear(month, year, pageable)
                .map(transactionMapper::entityToDto));
    }

    @Override
    public TransactionResponse findById(final String id) {
        log.debug("Fetching transaction with id: {}", id);
        return transactionMapper.entityToDto(transactionValidator.findTransactionOrThrow(id));
    }

    @Override
    public void delete(final String id) {
        log.info("Deleting transaction with id: {}", id);
        transactionRepository.delete(transactionValidator.findTransactionOrThrow(id));
    }

    @Override
    public PageResponse<TransactionResponse> findAllByUserId(final String userId, final Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(UserErrorMessages.USER_NOT_FOUND_WITH_ID + userId);
        }

        log.debug("Fetching transactions for user with id: {}", userId);
        return PageResponse.of(transactionRepository.findAllByUser_Id(userId, pageable)
                .map(transactionMapper::entityToDto));
    }

}
