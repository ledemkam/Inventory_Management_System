package com.kte.backend.catalog.validator;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.catalog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryValidator {

    private final CategoryRepository categoryRepository;

    /**
     * Checks if a category with the given name already exists in the repository.
     * If it does, an EntityAlreadyExistsException is thrown.
     *
     * @param name the name of the category to check
     * @throws EntityAlreadyExistsException if a category with the given name already exists
     */
    public void checkCategoryAlreadyExistsByName(final String name) {
        categoryRepository.findByNameIgnoreCase(name)
                .ifPresent(category -> {
                    log.error("Category with name {} already exists", name);
                    throw new EntityAlreadyExistsException(name);
                });
    }
}
