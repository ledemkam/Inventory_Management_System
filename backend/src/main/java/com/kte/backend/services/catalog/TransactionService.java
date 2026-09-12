package com.kte.backend.services.catalog;

import com.kte.backend.common.PageResponse;
import com.kte.backend.models.dto.request.TransactionRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.enums.TransactionStatus;
import com.kte.backend.services.CrudServices;
import org.springframework.data.domain.Pageable;

public interface TransactionService extends CrudServices<TransactionRequest, TransactionResponse, String> {
    TransactionResponse restockInventory(TransactionRequest transactionRequest);

    TransactionResponse sell(TransactionRequest transactionRequest);

    TransactionResponse returnToSupplier(TransactionRequest transactionRequest);

    TransactionResponse updateTransactionStatus(String transactionId, TransactionStatus transactionStatus);

    PageResponse<TransactionResponse> search(String searchText, Pageable pageable);

    PageResponse<TransactionResponse> searchByMonthAndYear(int month, int year, Pageable pageable);
}
