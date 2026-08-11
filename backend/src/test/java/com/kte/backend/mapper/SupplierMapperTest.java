package com.kte.backend.mapper;

import com.kte.backend.models.dto.request.SupplierRequest;
import com.kte.backend.models.dto.response.SupplierResponse;
import com.kte.backend.models.entity.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("SupplierMapper Test")
class SupplierMapperTest {

    @Autowired
    private SupplierMapper supplierMapper;

    @Test
    @DisplayName("Test entity to DTO mapping")
    void entity_To_Dto() {
        //Given
        Supplier supplier = Supplier.builder()
                .id("1L")
                .name("Tech Supplies")
                .address("123 Main Street")
                .build();

        //When
        SupplierResponse dto = supplierMapper.entityToDto(supplier);

        //Then
        assertNotNull(dto);

    }

    @Test
    @DisplayName("Test DTO to entity mapping")
    void dto_To_Entity() {
        //Given
        SupplierRequest supplierRequest = SupplierRequest.builder()
                .name("Tech Supplies")
                .address("123 Main Street")
                .build();

        //When
        Supplier entity = supplierMapper.dtoToEntity(supplierRequest);

        //Then
        assertNotNull(entity);

    }

    @Test
    @DisplayName("Test list of entities to list of DTOs mapping")
    void to_Dto_List() {
        //Given
        Supplier supplier1 = Supplier.builder()
                .name("Tech Supplies")
                .address("123 Main Street")
                .build();

        Supplier supplier2 = Supplier.builder()
                .name("Global Parts")
                .address("456 Second Avenue")
                .build();

        //When
        List<SupplierResponse> dtoList = supplierMapper.toDtoList(List.of(supplier1, supplier2));

        //Then
        assertNotNull(dtoList);

    }

    @Test
    @DisplayName("Test updating entity from DTO")
    void update_Entity_From_Dto() {
        //Given
        Supplier supplier = Supplier.builder()
                .id("1L")
                .name("Tech Supplies")
                .address("123 Main Street")
                .build();

        SupplierRequest supplierRequest = SupplierRequest.builder()
                .name("Global Parts")
                .address("456 Second Avenue")
                .build();

        //When
        supplierMapper.updateEntityFromDto(supplierRequest, supplier);

        //Then
        assertEquals("Global Parts", supplier.getName());
        assertEquals("456 Second Avenue", supplier.getAddress());
        assertEquals("1L", supplier.getId());
    }
}