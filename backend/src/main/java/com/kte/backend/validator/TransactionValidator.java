package com.kte.backend.validator;

import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.exception.NameValueRequiredException;
import com.kte.backend.models.dto.request.TransactionRequest;
import com.kte.backend.models.entity.Supplier;
import com.kte.backend.models.entity.Transaction;
import com.kte.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionValidator {

    private final TransactionRepository transactionRepository;
    private final SupplierValidator supplierValidator;

    /**
     * Finds a transaction by its ID or throws an EntityNotFoundException if not found.
     *
     * @param id the ID of the transaction to find
     * @return the found Transaction
     * @throws EntityNotFoundException if no transaction with the given ID is found
     */
    public Transaction findTransactionOrThrow(final String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Transaction with id {} not found", id);
                    return new EntityNotFoundException("Transaction not found");
                });
    }

    public int requireQuantity(final Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new NameValueRequiredException("Quantity must be a positive number");
        }
        return quantity;
    }

    public String requireSupplierId(final TransactionRequest request) {
        if (!StringUtils.hasText(request.supplierId())) {
            throw new NameValueRequiredException("Supplier id is required for this operation");
        }
        return request.supplierId();
    }

    public Supplier resolveOptionalSupplier(final TransactionRequest request) {
        return StringUtils.hasText(request.supplierId())
                ? supplierValidator.findSupplierOrThrow(request.supplierId())
                : null;
    }
}
