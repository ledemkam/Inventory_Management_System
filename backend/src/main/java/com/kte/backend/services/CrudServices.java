package com.kte.backend.services;

import com.kte.backend.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CrudServices<I, O, ID> {

    // I for request, O for response
    O create(final I request);

    O update(final ID id, final I request);

    PageResponse<O> findAll(final Pageable pageable);

    O findById(final ID id);

    void delete(final ID id);
}
