package com.kte.backend.catalog.internal.services;

import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.catalog.internal.dto.request.CategoryRequest;
import com.kte.backend.catalog.dto.response.CategoryResponse;
import com.kte.backend.catalog.Category;
import com.kte.backend.common.CrudServices;

public interface CategoryService extends CrudServices<CategoryRequest, CategoryResponse, String> {

    /**
     * Returns the {@link Category} aggregate for the given id, for collaborating
     * services (e.g. product creation) that need the entity itself rather than its
     * DTO representation.
     *
     * @param id the id of the category to load
     * @return the matching category
     * @throws EntityNotFoundException if no category exists with that id
     */
    Category findCategoryOrThrow(String id);
}
