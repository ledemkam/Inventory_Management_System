package com.kte.backend.controllers.impl;

import com.kte.backend.common.ImageUpload;
import com.kte.backend.controllers.UIProductController;
import com.kte.backend.models.dto.request.ProductRequest;
import com.kte.backend.models.dto.response.ProductResponse;
import com.kte.backend.common.PageResponse;
import com.kte.backend.services.catalog.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductController implements UIProductController {

    private final ProductService productService;

    @Override
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("product") @Valid final ProductRequest request,
            @RequestPart(value = "image", required = false) final MultipartFile image) {
        log.info("Received request to create product with sku: {}", request.sku());
        final ProductResponse createdProduct = productService.create(withImage(request, image));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @Override
    @PutMapping(path = "/{product-id}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("product-id") final String id,
            @RequestPart("product") @Valid final ProductRequest request,
            @RequestPart(value = "image", required = false) final MultipartFile image) {
        log.info("Received request to update product with id: {}", id);
        final ProductResponse updatedProduct = productService.update(id, withImage(request, image));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedProduct);
    }

    @Override
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(final Pageable pageable) {
        log.debug("Received request to get all products with pageable: {}", pageable);
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    @Override
    @GetMapping("/{product-id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("product-id") final String id) {
        log.debug("Received request to get product by id: {}", id);
        return ResponseEntity.ok(productService.findById(id));
    }

    @Override
    @DeleteMapping("/{product-id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("product-id") final String id) {
        log.info("Received request to delete product with id: {}", id);
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Folds the uploaded multipart file (when present) into the request as an
     * {@link ImageUpload}, so the service layer stays free of Spring's web types.
     * When no file is uploaded the request is returned untouched and its
     * {@code imageUrl} is used as-is.
     */
    private ProductRequest withImage(final ProductRequest request, final MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return request;
        }
        return ProductRequest.builder()
                .name(request.name())
                .sku(request.sku())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .expiryDate(request.expiryDate())
                .categoryId(request.categoryId())
                .image(toImageUpload(image))
                .build();
    }

    private ImageUpload toImageUpload(final MultipartFile file) {
        try {
            return new ImageUpload(file.getBytes(), file.getOriginalFilename(), file.getContentType());
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read uploaded product image", e);
        }
    }
}
