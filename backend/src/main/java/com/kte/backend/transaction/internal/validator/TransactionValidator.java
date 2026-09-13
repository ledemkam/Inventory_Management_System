package com.kte.backend.transaction.internal.validator;

import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.exception.NameValueRequiredException;
import com.kte.backend.transaction.internal.dto.request.TransactionRequest;
import com.kte.backend.catalog.Supplier;
import com.kte.backend.catalog.validator.SupplierValidator;
import com.kte.backend.transaction.Transaction;
import com.kte.backend.transaction.internal.repository.TransactionRepository;
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

    /**
     * Resolves the supplier for an operation that requires one, failing fast with a
     * {@link NameValueRequiredException} when the request carries no supplier id.
     */
    public Supplier requireSupplier(final TransactionRequest request) {
        return supplierValidator.findSupplierOrThrow(requireSupplierId(request));
    }

    /**
     * Resolves the supplier for an operation where it is optional, returning
     * {@code null} when the request carries no supplier id.
     */
    public Supplier resolveOptionalSupplier(final TransactionRequest request) {
        return StringUtils.hasText(request.supplierId())
                ? supplierValidator.findSupplierOrThrow(request.supplierId())
                : null;
    }

    private String requireSupplierId(final TransactionRequest request) {
        if (!StringUtils.hasText(request.supplierId())) {
            throw new NameValueRequiredException("Supplier id is required for this operation");
        }
        return request.supplierId();
    }
}
