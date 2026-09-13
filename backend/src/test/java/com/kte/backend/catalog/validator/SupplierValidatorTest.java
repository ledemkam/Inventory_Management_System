package com.kte.backend.catalog.validator;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.exception.EntityNotFoundException;
import com.kte.backend.catalog.Supplier;
import com.kte.backend.catalog.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierValidator Unit Tests")
class SupplierValidatorTest {

    @InjectMocks
    private SupplierValidator supplierValidator;

    @Mock
    private SupplierRepository supplierRepository;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder()
                .id("1")
                .name("Supplier A")
                .build();
    }

    @Test
    @DisplayName("checkSupplierAlreadyExistsByName: does nothing when no supplier with the name exists")
    void should_not_throw_when_supplier_name_is_free() {
        //GIVEN
        when(supplierRepository.findByNameIgnoreCase("Supplier A")).thenReturn(Optional.empty());

        //WHEN / THEN
        assertThatCode(() -> supplierValidator.checkSupplierAlreadyExistsByName("Supplier A"))
                .doesNotThrowAnyException();
        verify(supplierRepository).findByNameIgnoreCase("Supplier A");
    }

    @Test
    @DisplayName("checkSupplierAlreadyExistsByName: throws EntityAlreadyExistsException when the name is taken")
    void should_throw_when_supplier_name_already_exists() {
        //GIVEN
        when(supplierRepository.findByNameIgnoreCase("Supplier A")).thenReturn(Optional.of(supplier));

        //WHEN / THEN
        assertThatThrownBy(() -> supplierValidator.checkSupplierAlreadyExistsByName("Supplier A"))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage("Supplier A");
    }

    @Test
    @DisplayName("findSupplierOrThrow: returns the supplier when it exists")
    void should_return_supplier_when_found() {
        //GIVEN
        when(supplierRepository.findById("1")).thenReturn(Optional.of(supplier));

        //WHEN
        Supplier found = supplierValidator.findSupplierOrThrow("1");

        //THEN
        assertThat(found).isSameAs(supplier);
    }

    @Test
    @DisplayName("findSupplierOrThrow: throws EntityNotFoundException when the supplier is missing")
    void should_throw_when_supplier_not_found() {
        //GIVEN
        when(supplierRepository.findById("99")).thenReturn(Optional.empty());

        //WHEN / THEN
        assertThatThrownBy(() -> supplierValidator.findSupplierOrThrow("99"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Supplier not found");
    }
}
