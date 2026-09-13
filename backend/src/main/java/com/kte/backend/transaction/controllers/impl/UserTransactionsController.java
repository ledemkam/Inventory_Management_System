package com.kte.backend.transaction.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.transaction.controllers.UIUserTransactionsController;
import com.kte.backend.transaction.dto.response.TransactionResponse;
import com.kte.backend.transaction.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/users/transactions")
public class UserTransactionsController implements UIUserTransactionsController {

    private final TransactionService transactionService;

    @Override
    @GetMapping("/{user-id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PageResponse<TransactionResponse>> getUserAndTransactions(
            @PathVariable("user-id") final String id,
            final Pageable pageable
    ) {
        log.debug("Received request to get user and transactions for user-id: {}", id);
        final PageResponse<TransactionResponse> userWithTransactions = transactionService.findAllByUserId(id, pageable);
        return ResponseEntity.ok(userWithTransactions);
    }
}
