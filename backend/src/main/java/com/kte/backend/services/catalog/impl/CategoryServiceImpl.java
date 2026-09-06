package com.kte.backend.services.catalog.impl;

import com.kte.backend.validator.CategoryValidator;
import com.kte.backend.common.PageResponse;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.mapper.CategoryMapper;
import com.kte.backend.models.dto.request.CategoryRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.models.entity.Category;
import com.kte.backend.repository.CategoryRepository;
import com.kte.backend.services.catalog.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategoryValidator categoryValidator;


    @Override
    public CategoryResponse create(final CategoryRequest request) {
        categoryValidator.checkCategoryAlreadyExistsByName(request.name());
        final Category entity = categoryMapper.dtoToEntity(request);
        log.info("Creating new category with name: {}", entity.getName());
        final Category savedEntity = categoryRepository.save(entity);
        return categoryMapper.entityToDto(savedEntity);

    }

    @Override
    public CategoryResponse update(final String id, final CategoryRequest request) {
        log.info("Updating category with id: {}", id);
        final Category entity = findCategoryOrThrow(id);
        categoryMapper.updateEntityFromDto(request, entity);
        final Category updatedEntity = categoryRepository.save(entity);
        return categoryMapper.entityToDto(updatedEntity);
    }

    @Override
    public PageResponse<CategoryResponse> findAll(final Pageable pageable) {
        log.debug("Fetching all categories with pagination: page {}, size {}", pageable.getPageNumber(),
                pageable.getPageSize());
        return PageResponse.of(categoryRepository.findAll(pageable).map(categoryMapper::entityToDto));
    }

    @Override
    public CategoryResponse findById(final String id) {
        final Category entity = findCategoryOrThrow(id);
        log.info("Found category with id: {}", entity.getId());
        return categoryMapper.entityToDto(entity);
    }

    @Override
    public void delete(final String id) {
        final Category entity = findCategoryOrThrow(id);
        log.info("Deleting category with id: {}", entity.getId());
        categoryRepository.delete(entity);
    }

    @Override
    public Category findCategoryOrThrow(final String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Category with id {} not found", id);
                    return new EntityNotFoundException("Category not found");
                });
    }

}