package com.kte.backend.validator;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.models.entity.Product;
import com.kte.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductValidator {

    private final ProductRepository productRepository;

    /**
     * Checks if a product with the given sku already exists in the repository.
     * If it does, an EntityAlreadyExistsException is thrown.
     *
     * @param sku the sku of the product to check
     * @throws EntityAlreadyExistsException if a product with the given sku already exists
     */
    public void checkProductAlreadyExistsBySku(final String sku) {
        productRepository.findBySkuIgnoreCase(sku)
                .ifPresent(product -> {
                    log.error("Product with sku {} already exists", sku);
                    throw new EntityAlreadyExistsException(sku);
                });
    }

    /**
     * Finds a product by its ID or throws an EntityNotFoundException if not found.
     *
     * @param id the ID of the product to find
     * @return the found Product
     * @throws EntityNotFoundException if no product with the given ID is found
     */
    public Product findProductOrThrow(final String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with id {} not found", id);
                    return new EntityNotFoundException("Product not found");
                });
    }
}
