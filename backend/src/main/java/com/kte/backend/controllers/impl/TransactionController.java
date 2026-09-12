package com.kte.backend.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.controllers.UITransactionController;
import com.kte.backend.models.dto.request.TransactionRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/transactions")
public class TransactionController implements UITransactionController {
    @Override
    public ResponseEntity<TransactionResponse> purchase(TransactionRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<TransactionResponse> sell(TransactionRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<TransactionResponse> returnToSupplier(TransactionRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<PageResponse<TransactionResponse>> getAllTransactions(Pageable pageable) {
        return null;
    }

    @Override
    public ResponseEntity<TransactionResponse> getTransactionById(String id) {
        return null;
    }

    @Override
    public ResponseEntity<TransactionResponse> updateTransactionStatus(String id, TransactionStatus status) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteTransaction(String id) {
        return null;
    }
}
