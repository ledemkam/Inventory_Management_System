package com.kte.backend.transaction.internal.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.config.SecurityConfig;
import com.kte.backend.transaction.TransactionStatus;
import com.kte.backend.transaction.TransactionType;
import com.kte.backend.transaction.internal.dto.request.TransactionRequest;
import com.kte.backend.transaction.internal.dto.response.TransactionResponse;
import com.kte.backend.transaction.internal.services.TransactionService;
import com.kte.backend.user.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.when;

@WebMvcTest(TransactionController.class)
@AutoConfigureRestTestClient
@Import(SecurityConfig.class)
@DisplayName("TransactionController Test")
class TransactionControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    private TransactionRequest purchaseRequest;
    private TransactionRequest sellRequest;
    private TransactionRequest returnRequest;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        purchaseRequest = TransactionRequest.builder()
                .productId("p1")
                .quantity(5)
                .supplierId("s1")
                .description("Restock")
                .build();

        sellRequest = TransactionRequest.builder()
                .productId("p1")
                .quantity(3)
                .description("Sold to walk-in customer")
                .build();

        returnRequest = TransactionRequest.builder()
                .productId("p1")
                .quantity(2)
                .supplierId("s1")
                .description("Defective units")
                .build();

        transactionResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(2500))
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.PENDING)
                .description("Restock")
                .build();
    }

    // The security filter chain is stateless (SessionCreationPolicy.STATELESS), so the
    // SecurityContext is (re)established per-request by JwtAuthenticationFilter reading the
    // bearer token, not by a session/test-context shortcut such as @WithMockUser. Mock the
    // token validation so the filter grants the authority the endpoint requires.
    private void mockAuthenticatedRequest(final String role) {
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn(role);
    }

    private void mockAuthenticatedRequest() {
        mockAuthenticatedRequest("MANAGER");
    }

    @Test
    @DisplayName("should return created purchase transaction")
    @WithMockUser(roles = "MANAGER")
    void should_Return_Created_Purchase_Transaction() {
        //Given
        when(transactionService.restockInventory(purchaseRequest)).thenReturn(transactionResponse);
        mockAuthenticatedRequest();

        //When & Then
        restTestClient.post().uri("/api/v1/transactions/purchase")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(purchaseRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("t1")
                .jsonPath("$.totalProducts").isEqualTo(5)
                .jsonPath("$.transactionType").isEqualTo("PURCHASE")
                .jsonPath("$.status").isEqualTo("PENDING")
                .jsonPath("$.description").isEqualTo("Restock");
    }

    @Test
    @DisplayName("should return created sale transaction")
    @WithMockUser(roles = "MANAGER")
    void should_Return_Created_Sale_Transaction() {
        //Given
        final TransactionResponse sellResponse = TransactionResponse.builder()
                .id("t2")
                .totalProducts(3)
                .totalPrice(BigDecimal.valueOf(1500))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.PENDING)
                .description(sellRequest.description())
                .build();
        when(transactionService.sell(sellRequest)).thenReturn(sellResponse);
        mockAuthenticatedRequest();

        //When & Then
        restTestClient.post().uri("/api/v1/transactions/sell")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(sellRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("t2")
                .jsonPath("$.totalProducts").isEqualTo(3)
                .jsonPath("$.transactionType").isEqualTo("SALE")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    @DisplayName("should return created return-to-supplier transaction")
    @WithMockUser(roles = "MANAGER")
    void should_Return_Created_Return_To_Supplier_Transaction() {
        //Given
        final TransactionResponse returnResponse = TransactionResponse.builder()
                .id("t3")
                .totalProducts(2)
                .totalPrice(BigDecimal.valueOf(1000))
                .transactionType(TransactionType.RETURN_TO_SUPPLIER)
                .status(TransactionStatus.PENDING)
                .description(returnRequest.description())
                .build();
        when(transactionService.returnToSupplier(returnRequest)).thenReturn(returnResponse);
        mockAuthenticatedRequest();

        //When & Then
        restTestClient.post().uri("/api/v1/transactions/return-to-supplier")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(returnRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("t3")
                .jsonPath("$.totalProducts").isEqualTo(2)
                .jsonPath("$.transactionType").isEqualTo("RETURN_TO_SUPPLIER")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    @DisplayName("should return transactions matching the search text")
    @WithMockUser(roles = "MANAGER")
    void should_Search_Transactions() {
        //Given
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
        when(transactionService.search(any(), any())).thenReturn(transactionResponses);
        mockAuthenticatedRequest();

        //When & Then
        restTestClient.get().uri("/api/v1/transactions/search?search=restock")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo("t1")
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    @DisplayName("should reject transaction search for a non-manager/admin role")
    void should_Reject_Search_Transactions_When_Not_Manager_Or_Admin() {
        //Given
        mockAuthenticatedRequest("USER");

        //When & Then
        restTestClient.get().uri("/api/v1/transactions/search?search=restock")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("should return transactions for a given month and year")
    @WithMockUser(roles = "MANAGER")
    void should_Search_Transactions_By_Month_And_Year() {
        //Given
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
        when(transactionService.searchByMonthAndYear(eq(9), eq(2026), any())).thenReturn(transactionResponses);
        mockAuthenticatedRequest();

        //When & Then
        restTestClient.get().uri("/api/v1/transactions/search-by-month-and-year?month=9&year=2026")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo("t1")
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    @DisplayName("should return transaction by id")
    @WithMockUser(roles = "ADMIN")
    void should_Return_Transaction_By_Id() {
        //Given
        when(transactionService.findById("t1")).thenReturn(transactionResponse);
        mockAuthenticatedRequest("ADMIN");

        //When & Then
        restTestClient.get().uri("/api/v1/transactions/t1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("t1")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    @DisplayName("should reject getting a transaction by id for a non-manager/admin role")
    void should_Reject_Get_Transaction_By_Id_When_Not_Manager_Or_Admin() {
        //Given
        mockAuthenticatedRequest("USER");

        //When & Then
        restTestClient.get().uri("/api/v1/transactions/t1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("should update transaction status")
    @WithMockUser(roles = "MANAGER")
    void should_Update_Transaction_Status() {
        //Given
        final TransactionResponse completedResponse = TransactionResponse.builder()
                .id("t1")
                .totalProducts(5)
                .totalPrice(BigDecimal.valueOf(2500))
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.COMPLETED)
                .description("Restock")
                .build();
        when(transactionService.updateTransactionStatus("t1", TransactionStatus.COMPLETED)).thenReturn(completedResponse);
        mockAuthenticatedRequest();

        //When & Then
        restTestClient.patch().uri("/api/v1/transactions/update/t1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(TransactionStatus.COMPLETED)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("t1")
                .jsonPath("$.status").isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("should reject updating transaction status for a non-manager/admin role")
    void should_Reject_Update_Transaction_Status_When_Not_Manager_Or_Admin() {
        //Given
        mockAuthenticatedRequest("USER");

        //When & Then
        restTestClient.patch().uri("/api/v1/transactions/update/t1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(TransactionStatus.COMPLETED)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("should delete transaction")
    @WithMockUser(roles = "MANAGER")
    void should_Delete_Transaction() {
        //Given
        mockAuthenticatedRequest();

        //When & Then
        restTestClient.delete().uri("/api/v1/transactions/t1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("should reject deleting a transaction for a non-manager/admin role")
    void should_Reject_Delete_Transaction_When_Not_Manager_Or_Admin() {
        //Given
        mockAuthenticatedRequest("USER");

        //When & Then
        restTestClient.delete().uri("/api/v1/transactions/t1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isForbidden();
    }
}
