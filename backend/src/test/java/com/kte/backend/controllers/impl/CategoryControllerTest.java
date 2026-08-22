package com.kte.backend.controllers.impl;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kte.backend.common.PageResponse;
import com.kte.backend.config.SecurityConfig;
import com.kte.backend.mapper.CategoryMapper;
import com.kte.backend.models.dto.request.CategoryRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.Category;
import com.kte.backend.security.JwtTokenService;
import com.kte.backend.services.catalog.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;


@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@DisplayName("CategoryController Test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CategoryMapper categoryMapper;

    private ObjectMapper objectMapper;

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
        objectMapper = new ObjectMapper();


    }

    @Test
    @DisplayName("Should return created category")
    @WithMockUser(roles = "MANAGER")
    void should_Return_created_Category() throws Exception {
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
        mockMvc.perform(post("/api/v1/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("Electronics")));
    }

    @Test
    @DisplayName("Should reject category creation for no-manager role")
    void should_Reject_created_Category_When_Not_Manager() throws Exception {
        //GIVEN
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("ADMIN");
        //WHEN & THEN
        mockMvc.perform(post("/api/v1/categories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return updated category")
    @WithMockUser(roles = "MANAGER")
    void should_Return_Updated_Category() throws Exception {
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
        mockMvc.perform(put("/api/v1/categories/" + categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id", is(categoryId)))
                .andExpect(jsonPath("$.name", is("ElectronicsUpdated")));
    }

    @Test
    @DisplayName("Should return updated category when no manager")
    void should_Return_Updated_Category_When_No_Manager() throws Exception {
        //GIVEN
        String categoryId = "1";
        when(categoryService.update(categoryId, categoryRequest)).thenReturn(updatedCategoryResponse);
        // The security filter chain is stateless (SessionCreationPolicy.STATELESS), so the
        // SecurityContext is (re)established per-request by JwtAuthenticationFilter reading the
        // bearer token, not by a session/test-context shortcut such as @WithMockUser. Mock the
        // token validation so the filter grants the MANAGER authority the endpoint requires.
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("ADMIN");
        //WHEN & THEN
        mockMvc.perform(put("/api/v1/categories/" + categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("Should return all categories")
    void should_Return_All_Categories() throws Exception {
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
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryResponses)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is("1")))
                .andExpect(jsonPath("$.content[0].name", is("Electronics")))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("Should return category by id")
    void should_Return_Category_By_Id() throws Exception {
        String categoryId = "1";
        when(categoryService.findById(categoryId)).thenReturn(categoryResponse);
        //WHEN & THEN
        mockMvc.perform(get("/api/v1/categories/" + categoryId)
                        .content(objectMapper.writeValueAsString(categoryResponse))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoryId)))
                .andExpect(jsonPath("$.name", is("Electronics")));
    }

    @Test
    @DisplayName("Should delete category")
    @WithMockUser(roles = "MANAGER")
    void should_Delete_Category() throws Exception {
        String categoryId = "1";
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("MANAGER");
        //WHEN & THEN
        mockMvc.perform(delete("/api/v1/categories/" + categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .content(objectMapper.writeValueAsString(categoryResponse))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Should delete category when no manager")
    void should_Delete_Category_When_No_Manager() throws Exception {
        String categoryId = "1";
        when(jwtTokenService.validateToken(anyString())).thenReturn(true);
        when(jwtTokenService.getUserIdFromTokEN(anyString())).thenReturn("1");
        when(jwtTokenService.getRoleFromToken(anyString())).thenReturn("ADMIN");
        //WHEN & THEN
        mockMvc.perform(delete("/api/v1/categories/" + categoryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                        .content(objectMapper.writeValueAsString(categoryResponse))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

    }
}