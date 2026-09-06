package com.kte.backend.services.catalog;

import com.kte.backend.models.dto.request.ProductRequest;
import com.kte.backend.models.dto.response.ProductResponse;
import com.kte.backend.services.CrudServices;

/**
 * CRUD operations for products.
 * <p>
 * Image handling rides along on {@link ProductRequest#image()}: when it carries
 * bytes they are stored and the resulting URL is used; otherwise
 * {@link ProductRequest#imageUrl()} is kept as-is. No separate method is needed.
 */
public interface ProductService extends CrudServices<ProductRequest, ProductResponse, String> {
}
