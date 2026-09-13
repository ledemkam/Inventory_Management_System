package com.kte.backend.transaction.internal.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.transaction.internal.controllers.UITransactionController;
import com.kte.backend.transaction.internal.dto.request.TransactionRequest;
import com.kte.backend.transaction.internal.dto.response.TransactionResponse;
import com.kte.backend.transaction.TransactionStatus;
import com.kte.backend.transaction.internal.services.TransactionService;
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
    @GetMapping("/search")
    public ResponseEntity<PageResponse<TransactionResponse>> searchTransactions(
            @RequestParam(name = "search", required = false) final String searchText, final Pageable pageable) {
        log.debug("Received request to search transactions with searchText '{}' and pageable: {}",
                searchText, pageable);
        PageResponse<TransactionResponse> response = transactionService.search(searchText, pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/search-by-month-and-year")
    public ResponseEntity<PageResponse<TransactionResponse>> searchTransactionsByMonthAndYear(
            @RequestParam(name = "month") final int month,
            @RequestParam(name = "year") final int year,
            final Pageable pageable) {
        log.debug("Received request to search transactions by month '{}' and year '{}' with pageable: {}", month, year, pageable);
        PageResponse<TransactionResponse> response = transactionService.searchByMonthAndYear(month, year, pageable);
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
    @PatchMapping("/update/{transaction-id}")
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @PathVariable("transaction-id") final String id,
            @RequestBody final TransactionStatus status) {
        TransactionResponse updatedTransaction = transactionService.updateTransactionStatus(id, status);
        return ResponseEntity.ok(updatedTransaction);
    }

    @Override
    @DeleteMapping("/{transaction-id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("transaction-id") final String id) {
        transactionService.delete(id);
        log.info("Received request to delete transaction with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
