package com.kte.backend.catalog.internal.services;

import com.kte.backend.catalog.internal.dto.request.ProductRequest;
import com.kte.backend.catalog.dto.response.ProductResponse;
import com.kte.backend.common.CrudServices;

/**
 * CRUD operations for products.
 * <p>
 * Image handling rides along on {@link ProductRequest#image()}: when it carries
 * bytes they are stored and the resulting URL is used; otherwise
 * {@link ProductRequest#imageUrl()} is kept as-is. No separate method is needed.
 */
public interface ProductService extends CrudServices<ProductRequest, ProductResponse, String> {
}
