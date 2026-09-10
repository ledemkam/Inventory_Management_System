package com.kte.backend.validator;

import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.models.entity.Transaction;
import com.kte.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionValidator {

    private final TransactionRepository transactionRepository;

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
}
