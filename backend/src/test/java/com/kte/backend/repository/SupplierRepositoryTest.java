package com.kte.backend.repository;

import com.kte.backend.models.entity.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
@DisplayName("SupplierRepository Test")
class SupplierRepositoryTest {

    @Autowired
    private SupplierRepository supplierRepository;

    PageRequest pageRequest = PageRequest.of(0, 10);


    @Test
    @DisplayName("Find supplier by name ignoring case")
    void findByNameIgnoreCase_ReturnsSupplier_WhenNameMatchesIgnoringCase() {
        //Given
        Supplier supplier = supplierRepository.save(
                Supplier.builder()
                        .name("Global Supplies Ltd")
                        .address("123 Industrial Ave")
                        .build()
        );

        //When
        Optional<Supplier> found = supplierRepository.findByNameIgnoreCase("global supplies ltd");

        //Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(supplier.getId());
    }

    @Test
    @DisplayName("Return empty when no supplier matches the given name")
    void findByNameIgnoreCase_ReturnsEmpty_WhenNoSupplierMatches() {
        //When
        Optional<Supplier> found = supplierRepository.findByNameIgnoreCase("Nonexistent Supplier");

        //Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Find all suppliers returns a list of suppliers")
    void find_All_Supplier() {
        //Given
        Supplier supplier1 = Supplier.builder()
                .name("Global Supplies Ltd")
                .address("123 Industrial Ave")
                .build();
        Supplier supplier2 = Supplier.builder()
                .name("Local Supplies Inc")
                .address("456 Market St")
                .build();
        supplierRepository.save(supplier1);
        supplierRepository.save(supplier2);

        //When
        Page<Supplier> suppliers = supplierRepository.findAll(pageRequest);

        //Then
        assertThat(suppliers).isNotEmpty();
    }

    @Test
    @DisplayName("Find supplier by id")
    void find_Supplier_By_Id() {
        //Given
        Supplier supplier = Supplier.builder()
                .name("Global Supplies Ltd")
                .address("123 Industrial Ave")
                .build();
        Supplier savedSupplier = supplierRepository.save(supplier);

        //When
        Optional<Supplier> foundSupplier = supplierRepository.findById(savedSupplier.getId());

        //Then
        assertThat(foundSupplier).isPresent();
        assertThat(foundSupplier.get().getId()).isEqualTo(savedSupplier.getId());
    }

    @Test
    @DisplayName("Delete supplier by id")
    void delete_Supplier_By_Id() {
        //Given
        Supplier supplier = Supplier.builder()
                .name("Global Supplies Ltd")
                .address("123 Industrial Ave")
                .build();
        Supplier savedSupplier = supplierRepository.save(supplier);

        //When
        supplierRepository.deleteById(savedSupplier.getId());

        //Then
        Optional<Supplier> deletedSupplier = supplierRepository.findById(savedSupplier.getId());
        assertThat(deletedSupplier).isEmpty();
    }
}