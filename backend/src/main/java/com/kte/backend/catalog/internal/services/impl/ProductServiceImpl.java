package com.kte.backend.catalog.internal.services.impl;

import com.kte.backend.common.ImageUpload;
import com.kte.backend.common.PageResponse;
import com.kte.backend.catalog.internal.mapper.ProductMapper;
import com.kte.backend.catalog.internal.dto.request.ProductRequest;
import com.kte.backend.catalog.dto.response.ProductResponse;
import com.kte.backend.catalog.Category;
import com.kte.backend.catalog.Product;
import com.kte.backend.catalog.repository.ProductRepository;
import com.kte.backend.catalog.internal.services.CategoryService;
import com.kte.backend.catalog.internal.services.ProductService;
import com.kte.backend.catalog.internal.storage.FileStorageService;
import com.kte.backend.catalog.validator.ProductValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductValidator productValidator;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    @Override
    public ProductResponse create(final ProductRequest request) {
        productValidator.checkProductAlreadyExistsBySku(request.sku());
        final Category category = categoryService.findCategoryOrThrow(request.categoryId());

        final Product entity = productMapper.dtoToEntity(request);
        entity.setCategory(category);
        applyImageIfPresent(entity, request.image());

        log.info("Creating new product with sku: {}", entity.getSku());
        final Product savedEntity = productRepository.save(entity);
        return productMapper.entityToDto(savedEntity);
    }


    @Override
    public ProductResponse update(final String id, final ProductRequest request) {
        final Product entity = productValidator.findProductOrThrow(id);
        final Category category = categoryService.findCategoryOrThrow(request.categoryId());

        productMapper.updateEntityFromDto(request, entity);
        entity.setCategory(category);
        applyImageIfPresent(entity, request.image());

        log.info("Updating product with id: {}", entity.getId());
        final Product updatedEntity = productRepository.save(entity);
        return productMapper.entityToDto(updatedEntity);
    }

    @Override
    public PageResponse<ProductResponse> findAll(final Pageable pageable) {
        log.debug("Fetching all products with pagination: page {}, size {}", pageable.getPageNumber(),
                pageable.getPageSize());
        return PageResponse.of(productRepository.findAll(pageable).map(productMapper::entityToDto));
    }

    @Override
    public ProductResponse findById(final String id) {
        log.debug("Fetching product with id: {}", id);
        final Product entity = productValidator.findProductOrThrow(id);
        return productMapper.entityToDto(entity);
    }

    @Override
    public void delete(final String id) {
        log.info("Deleting product with id: {}", id);
        final Product entity = productValidator.findProductOrThrow(id);
        productRepository.delete(entity);
    }


    /**
     * Uploads the given image and sets it as the product's image, when one was provided.
     * If no image is provided, whatever imageUrl the mapper already applied from the
     * request (e.g. an external URL) is left untouched.
     */
    private void applyImageIfPresent(final Product entity, final ImageUpload image) {
        if (image != null && !image.isEmpty()) {
            entity.setImageUrl(fileStorageService.store(image));
        }
    }
}
