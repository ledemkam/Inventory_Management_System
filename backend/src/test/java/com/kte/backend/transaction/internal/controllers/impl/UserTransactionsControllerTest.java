package com.kte.backend.transaction.internal.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.config.SecurityConfig;
import com.kte.backend.transaction.TransactionStatus;
import com.kte.backend.transaction.TransactionType;
import com.kte.backend.transaction.internal.dto.response.TransactionResponse;
import com.kte.backend.transaction.internal.services.TransactionService;
import com.kte.backend.user.security.JwtTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(UserTransactionsController.class)
@AutoConfigureRestTestClient
@Import(SecurityConfig.class)
@DisplayName("web layer test for UserTransactionsController")
class UserTransactionsControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Test
    @WithMockUser(roles = {"MANAGER", "ADMIN"})
    @DisplayName("should return a user's transactions")
    void should_Return_User_Transactions() {
        final TransactionResponse transactionResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(2)
                .totalPrice(BigDecimal.valueOf(100))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .build();

        final PageResponse<TransactionResponse> transactionResponses = PageResponse.<TransactionResponse>builder()
                .content(List.of(transactionResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();

        when(transactionService.findAllByUserId(eq("1"), any())).thenReturn(transactionResponses);

        // getUserAndTransactions is granted to ADMIN and MANAGER (see hasAnyRole on the
        // controller), so this test exercises the MANAGER path specifically.
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("2");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");

        restTestClient.get().uri("/api/v1/users/transactions/{user-id}", "1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo("t1")
                .jsonPath("$.totalElements").isEqualTo(1);

        verify(transactionService, times(1)).findAllByUserId(eq("1"), any());
    }
}
