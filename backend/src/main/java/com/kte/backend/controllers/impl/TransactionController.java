package com.kte.backend.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.controllers.UITransactionController;
import com.kte.backend.models.dto.request.TransactionRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.enums.TransactionStatus;
import com.kte.backend.services.catalog.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/transactions")
public class TransactionController implements UITransactionController {
    private final TransactionService transactionService;

    @Override
    @PostMapping("/purchase")
    public ResponseEntity<TransactionResponse> purchase(
            @Valid
            @RequestBody final TransactionRequest request) {
        log.info("Received request to record a purchase transaction: {}", request);
        TransactionResponse newTransaction = transactionService.restockInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTransaction);
    }

    @Override
    @PostMapping("/sell")
    public ResponseEntity<TransactionResponse> sell(@Valid @RequestBody final TransactionRequest request) {
        log.info("Received request to record a sell transaction: {}", request);
        TransactionResponse newTransaction = transactionService.sell(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTransaction);
    }

    @Override
    @PostMapping("/return-to-supplier")
    public ResponseEntity<TransactionResponse> returnToSupplier(@Valid @RequestBody final TransactionRequest request) {
        log.info("Received request to record a return to supplier transaction: {}", request);
        TransactionResponse newTransaction = transactionService.returnToSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTransaction);
    }

    @Override
    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> getAllTransactions(final Pageable pageable) {
        PageResponse<TransactionResponse> response = transactionService.findAll(pageable);
        log.debug("Received request to get all transactions with pageable: {}", pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{transaction-id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable("transaction-id") final String id) {
        TransactionResponse transaction = transactionService.findById(id);
        log.debug("Received request to get transaction by id: {}", id);
        return ResponseEntity.ok(transaction);
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
