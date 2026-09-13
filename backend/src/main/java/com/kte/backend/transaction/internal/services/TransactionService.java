package com.kte.backend.transaction.internal.services;

import com.kte.backend.common.PageResponse;
import com.kte.backend.transaction.internal.dto.request.TransactionRequest;
import com.kte.backend.transaction.internal.dto.response.TransactionResponse;
import com.kte.backend.transaction.TransactionStatus;
import com.kte.backend.common.CrudServices;
import org.springframework.data.domain.Pageable;

public interface TransactionService extends CrudServices<TransactionRequest, TransactionResponse, String> {
    TransactionResponse restockInventory(TransactionRequest transactionRequest);

    TransactionResponse sell(TransactionRequest transactionRequest);

    TransactionResponse returnToSupplier(TransactionRequest transactionRequest);

    TransactionResponse updateTransactionStatus(String transactionId, TransactionStatus transactionStatus);

    PageResponse<TransactionResponse> search(String searchText, Pageable pageable);

    PageResponse<TransactionResponse> searchByMonthAndYear(int month, int year, Pageable pageable);

    /**
     * Returns the transactions recorded for the given user.
     *
     * @param userId the id of the user whose transactions are fetched
     * @throws com.kte.backend.exception.EntityNotFoundException if no user exists with that id
     */
    PageResponse<TransactionResponse> findAllByUserId(String userId, Pageable pageable);
}
