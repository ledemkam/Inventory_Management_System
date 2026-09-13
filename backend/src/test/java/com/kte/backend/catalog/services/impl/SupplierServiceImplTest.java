package com.kte.backend.catalog.services.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.catalog.mapper.SupplierMapper;
import com.kte.backend.catalog.dto.request.SupplierRequest;
import com.kte.backend.catalog.dto.response.SupplierResponse;
import com.kte.backend.catalog.Supplier;
import com.kte.backend.catalog.repository.SupplierRepository;
import com.kte.backend.catalog.validator.SupplierValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@DisplayName("SupplierServiceImpl Unit Tests")
class SupplierServiceImplTest {

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierValidator supplierValidator;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier supplier;
    private SupplierRequest supplierRequest;
    private SupplierResponse supplierResponse;

    @BeforeEach
    void setup() {
        supplier = Supplier.builder()
                .id("1")
                .name("Supplier A")
                .build();

        supplierRequest = SupplierRequest.builder()
                .name("Supplier A")
                .build();

        supplierResponse = SupplierResponse.builder()
                .id("1")
                .name("Supplier A")
                .build();
    }


    @Test
    @DisplayName("should create supplier when it does not exist")
    void should_create_supplier_when_no_exist() {
        //Given
        when(supplierMapper.dtoToEntity(supplierRequest)).thenReturn(supplier);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.entityToDto(supplier)).thenReturn(supplierResponse);
        doNothing().when(supplierValidator).checkSupplierAlreadyExistsByName(supplierRequest.name());


        //When
        SupplierResponse response = supplierService.create(supplierRequest);

        //Then
        assertThat(response).isNotNull();
    }


    @Test
    @DisplayName("should update supplier when it exists")
    void should_Update_supplier_when_it_exists() {
        //Given
        when(supplierValidator.findSupplierOrThrow(supplier.getId())).thenReturn(supplier);
        when(supplierMapper.dtoToEntity(supplierRequest)).thenReturn(supplier);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.entityToDto(supplier)).thenReturn(supplierResponse);
        //When
        SupplierResponse response = supplierService.update(supplier.getId(), supplierRequest);
        //Then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("should find all suppliers")
    void should_find_All_suppliers() {
        //Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Supplier> supplierPage = new PageImpl<>(List.of(supplier), pageable, 1);
        when(supplierRepository.findAll(pageable)).thenReturn(supplierPage);
        when(supplierMapper.entityToDto(supplier)).thenReturn(supplierResponse);
        //When
        PageResponse<SupplierResponse> response = supplierService.findAll(pageable);

        //Then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("should find supplier by id")
    void should_find_supplier_by_id() {
        //Given
        when(supplierValidator.findSupplierOrThrow(supplier.getId())).thenReturn(supplier);
        when(supplierMapper.entityToDto(supplier)).thenReturn(supplierResponse);
        //When
        SupplierResponse response = supplierService.findById(supplier.getId());
        //Then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("should delete supplier by id")
    void should_delete_supplier_by_id() {
        //Given
        when(supplierValidator.findSupplierOrThrow(supplier.getId())).thenReturn(supplier);
        doNothing().when(supplierRepository).delete(supplier);
        //When
        supplierService.delete(supplier.getId());
        //Then
        verify(supplierRepository, times(1)).delete(supplier);
    }
}