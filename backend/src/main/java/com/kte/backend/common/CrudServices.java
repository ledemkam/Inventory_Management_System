package com.kte.backend.common;


import org.springframework.data.domain.Pageable;

/**
 * A generic interface for CRUD (Create, Read, Update, Delete) operations.
 *
 * @param <I> the type of the request object
 * @param <O> the type of the response object
 * @param <K> the type of the entity ID
 */

public interface CrudServices<I, O, K> {

    // I for request, O for response

    O create(final I request);

    O update(final K id, final I request);

    PageResponse<O> findAll(final Pageable pageable);

    O findById(final K id);

    void delete(final K id);
}
