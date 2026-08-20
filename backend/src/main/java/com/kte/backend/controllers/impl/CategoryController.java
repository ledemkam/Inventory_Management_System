package com.kte.backend.controllers.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.controllers.UICategoryController;
import com.kte.backend.models.dto.request.CategoryRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.services.catalog.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/categories")
public class CategoryController implements UICategoryController {

    private final CategoryService categoryService;


    @Override
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody
            @Valid final CategoryRequest request) {
        log.info("Received request to create category with request: {}", request);
        categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PutMapping("/{category-id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("category-id") final String id,
                                                           @RequestBody @Valid final CategoryRequest request) {
        log.info("Received request to update category with id: {} and request: {}", id, request);
        CategoryResponse updatedCategory = categoryService.update(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedCategory);
    }

    @Override
    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(final Pageable pageable) {
        log.debug("Received request to get all categories with pageable: {}", pageable);
        PageResponse<CategoryResponse> response = categoryService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CategoryResponse> getCategoryById(final String id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteCategory(final String id) {
        return null;
    }
}
