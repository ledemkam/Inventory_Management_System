package com.kte.backend.services.catalog;

import com.kte.backend.models.dto.request.TransactionRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.enums.TransactionStatus;
import com.kte.backend.services.CrudServices;

public interface TransactionService extends CrudServices<TransactionRequest, TransactionResponse, String> {
    TransactionResponse restockInventory(TransactionRequest transactionRequest);

    TransactionResponse sell(TransactionRequest transactionRequest);

    TransactionResponse returnToSupplier(TransactionRequest transactionRequest);

    TransactionResponse updateTransactionStatus(Long transactionId, TransactionStatus transactionStatus);
}
