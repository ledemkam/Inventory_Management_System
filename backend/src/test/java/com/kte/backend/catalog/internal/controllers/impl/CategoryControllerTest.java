package com.kte.backend.catalog.internal.controllers.impl;


import com.kte.backend.common.PageResponse;
import com.kte.backend.config.SecurityConfig;
import com.kte.backend.catalog.internal.mapper.CategoryMapper;
import com.kte.backend.catalog.internal.dto.request.CategoryRequest;
import com.kte.backend.catalog.dto.response.CategoryResponse;
import com.kte.backend.catalog.Category;
import com.kte.backend.user.security.JwtTokenService;
import com.kte.backend.catalog.internal.services.CategoryService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@WebMvcTest(CategoryController.class)
@AutoConfigureRestTestClient
@Import(SecurityConfig.class)
@DisplayName("CategoryController Test")
class CategoryControllerTest {

    @Autowired
    private RestTestClient restTestClient;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CategoryMapper categoryMapper;

    private Category category;
    private CategoryResponse categoryResponse;
    private CategoryRequest categoryRequest;
    private CategoryResponse updatedCategoryResponse;

    @BeforeEach
    void setUp() {
        String categoryId = "1";
        category = Category.builder()
                .id(categoryId)
                .name("Electronics")
                .build();
        categoryResponse = CategoryResponse.builder()
                .id(categoryId)
                .name("Electronics")
                .build();
        categoryRequest = CategoryRequest.builder()
                .name("Electronics")
                .build();
        updatedCategoryResponse = CategoryResponse.builder()
                .id(categoryId)
                .name("ElectronicsUpdated")
                .build();
    }

    @Test
    @DisplayName("Should return created category")
    @WithMockUser(roles = "MANAGER")
    void should_Return_created_Category() {
        //GIVEN
        when(categoryService.create(categoryRequest)).thenReturn(categoryResponse);
        // The security filter chain is stateless (SessionCreationPolicy.STATELESS), so the
        // SecurityContext is (re)established per-request by JwtAuthenticationFilter reading the
        // bearer token, not by a session/test-context shortcut such as @WithMockUser. Mock the
        // token validation so the filter grants the MANAGER authority the endpoint requires.
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");
        //WHEN & THEN
        restTestClient.post().uri("/api/v1/categories")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(categoryRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.name").isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should reject category creation for no-manager role")
    void should_Reject_created_Category_When_Not_Manager() {
        //GIVEN
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("USER");
        //WHEN & THEN
        restTestClient.post().uri("/api/v1/categories")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(categoryRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Should let ADMIN create a category through the role hierarchy (ADMIN > MANAGER)")
    void should_Return_created_Category_When_Admin() {
        //GIVEN
        when(categoryService.create(categoryRequest)).thenReturn(categoryResponse);
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("ADMIN");
        //WHEN & THEN
        restTestClient.post().uri("/api/v1/categories")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(categoryRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1");
    }

    @Test
    @DisplayName("Should return updated category")
    @WithMockUser(roles = "MANAGER")
    void should_Return_Updated_Category() {
        //GIVEN
        String categoryId = "1";
        when(categoryService.update(categoryId, categoryRequest)).thenReturn(updatedCategoryResponse);
        // The security filter chain is stateless (SessionCreationPolicy.STATELESS), so the
        // SecurityContext is (re)established per-request by JwtAuthenticationFilter reading the
        // bearer token, not by a session/test-context shortcut such as @WithMockUser. Mock the
        // token validation so the filter grants the MANAGER authority the endpoint requires.
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");
        //WHEN & THEN
        restTestClient.put().uri("/api/v1/categories/" + categoryId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(categoryRequest)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody()
                .jsonPath("$.id").isEqualTo(categoryId)
                .jsonPath("$.name").isEqualTo("ElectronicsUpdated");
    }

    @Test
    @DisplayName("Should return updated category when no manager")
    void should_Return_Updated_Category_When_No_Manager() {
        //GIVEN
        String categoryId = "1";
        when(categoryService.update(categoryId, categoryRequest)).thenReturn(updatedCategoryResponse);
        // The security filter chain is stateless (SessionCreationPolicy.STATELESS), so the
        // SecurityContext is (re)established per-request by JwtAuthenticationFilter reading the
        // bearer token, not by a session/test-context shortcut such as @WithMockUser. Mock the
        // token validation so the filter grants the MANAGER authority the endpoint requires.
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("USER");
        //WHEN & THEN
        restTestClient.put().uri("/api/v1/categories/" + categoryId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(categoryRequest)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Should return all categories")
    void should_Return_All_Categories() {
        //GIVEN
        final PageResponse<CategoryResponse> categoryResponses = PageResponse.<CategoryResponse>builder()
                .content(List.of(categoryResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFirst(true)
                .isLast(true)
                .build();
        when(categoryService.findAll(any())).thenReturn(categoryResponses);
        //WHEN & THEN
        restTestClient.get().uri("/api/v1/categories")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo("1")
                .jsonPath("$.content[0].name").isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should return category by id")
    void should_Return_Category_By_Id() {
        String categoryId = "1";
        when(categoryService.findById(categoryId)).thenReturn(categoryResponse);
        //WHEN & THEN
        restTestClient.get().uri("/api/v1/categories/" + categoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(categoryId)
                .jsonPath("$.name").isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should delete category")
    @WithMockUser(roles = "MANAGER")
    void should_Delete_Category() {
        String categoryId = "1";
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");
        //WHEN & THEN
        restTestClient.delete().uri("/api/v1/categories/" + categoryId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Should delete category when no manager")
    void should_Delete_Category_When_No_Manager() {
        String categoryId = "1";
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("USER");
        //WHEN & THEN
        restTestClient.delete().uri("/api/v1/categories/" + categoryId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .exchange()
                .expectStatus().isForbidden();
    }
}
