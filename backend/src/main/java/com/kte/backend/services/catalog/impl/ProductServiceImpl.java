package com.kte.backend.services.catalog.impl;

import com.kte.backend.common.ImageUpload;
import com.kte.backend.common.PageResponse;
import com.kte.backend.mapper.ProductMapper;
import com.kte.backend.models.dto.request.ProductRequest;
import com.kte.backend.models.dto.response.ProductResponse;
import com.kte.backend.models.entity.Category;
import com.kte.backend.models.entity.Product;
import com.kte.backend.repository.ProductRepository;
import com.kte.backend.services.catalog.CategoryService;
import com.kte.backend.services.catalog.ProductService;
import com.kte.backend.services.storage.FileStorageService;
import com.kte.backend.validator.ProductValidator;
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
        return null;
    }

    @Override
    public ProductResponse findById(final String id) {
        return null;
    }

    @Override
    public void delete(final String id) {

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
