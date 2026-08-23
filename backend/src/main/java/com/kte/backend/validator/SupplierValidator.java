package com.kte.backend.validator;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.models.entity.Supplier;
import com.kte.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupplierValidator {

    private final SupplierRepository supplierRepository;

    /**
     * Checks if a supplier with the given name already exists in the repository.
     * If it does, an EntityAlreadyExistsException is thrown.
     *
     * @param name the name of the supplier to check
     * @throws EntityAlreadyExistsException if a supplier with the given name already exists
     */
    public void checkSupplierAlreadyExistsByName(final String name) {
        supplierRepository.findByNameIgnoreCase(name)
                .ifPresent(supplier -> {
                    log.error("Supplier with name {} already exists", name);
                    throw new EntityAlreadyExistsException(name);
                });
    }

    /**
     * Finds a supplier by its ID or throws an EntityNotFoundException if not found.
     *
     * @param id the ID of the supplier to find
     * @return the found Supplier
     * @throws EntityNotFoundException if no supplier with the given ID is found
     */
    public Supplier findSupplierOrThrow(final String id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Supplier with id {} not found", id);
                    return new EntityNotFoundException("Supplier not found");
                });
    }
}
