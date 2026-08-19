package com.kte.backend.services.catalog.impl;

import com.kte.backend.Validator.CategoryValidator;
import com.kte.backend.mapper.CategoryMapper;
import com.kte.backend.models.dto.request.CategoryRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.models.entity.Category;
import com.kte.backend.repository.CategoryRepository;
import com.kte.backend.services.catalog.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
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
    public CategoryResponse findById(final String id) {
        final Category entity = categoryValidator.findCategoryOrThrow(id);
        log.info("Found category with id: {}", entity.getId());
        return categoryMapper.entityToDto(entity);
    }


}