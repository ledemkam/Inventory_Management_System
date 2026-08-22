package com.kte.backend.Validator;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.models.entity.Category;
import com.kte.backend.repository.CategoryRepository;
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

    /**
     * Finds a category by its ID or throws an EntityNotFoundException if not found.
     *
     * @param id the ID of the category to find
     * @return the found Category
     * @throws EntityNotFoundException if no category with the given ID is found
     */
    public Category findCategoryOrThrow(final String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Category with id {} not found", id);
                    return new EntityNotFoundException("Category not found");
                });
    }
}
