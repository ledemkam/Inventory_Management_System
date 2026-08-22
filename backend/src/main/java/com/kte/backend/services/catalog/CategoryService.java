package com.kte.backend.services.catalog;

import com.kte.backend.models.dto.request.CategoryRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.services.CrudServices;

public interface CategoryService extends CrudServices<CategoryRequest, CategoryResponse, String> {
}
