package com.kte.backend.transaction.internal.controllers;

import com.kte.backend.common.PageResponse;
import com.kte.backend.exception.Error;
import com.kte.backend.transaction.internal.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

/**
 * Exposes a user's transactions. Lives in the transaction module (rather than the user
 * module) so that transactions stay the owner of transaction data - the user module has
 * no need to depend on it.
 */
@Tag(name = "User Controller", description = "Endpoints for managing users")
public interface UIUserTransactionsController {

    @Operation(summary = "Get user and their transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = Error.class))),
    })
    ResponseEntity<PageResponse<TransactionResponse>> getUserAndTransactions(final String id, final Pageable pageable);
}
