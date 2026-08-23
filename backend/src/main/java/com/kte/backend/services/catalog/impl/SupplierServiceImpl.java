package com.kte.backend.services.catalog.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.mapper.SupplierMapper;
import com.kte.backend.models.dto.request.SupplierRequest;
import com.kte.backend.models.dto.response.SupplierResponse;
import com.kte.backend.models.entity.Supplier;
import com.kte.backend.repository.SupplierRepository;
import com.kte.backend.services.catalog.SupplierService;
import com.kte.backend.validator.SupplierValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.HikariCheckpointRestoreLifecycle;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final SupplierValidator supplierValidator;

    @Override
    public SupplierResponse create(final SupplierRequest request) {
        supplierValidator.checkSupplierAlreadyExistsByName(request.name());
        final Supplier entity = supplierMapper.dtoToEntity(request);
        log.info("Creating new supplier with name: {}", entity.getName());
        final Supplier savedEntity = supplierRepository.save(entity);
        return supplierMapper.entityToDto(savedEntity);
    }


    @Override
    public SupplierResponse update(final String id, final SupplierRequest request) {
        log.info("Updating supplier with id: {}", id);
        supplierValidator.findSupplierOrThrow(id);
        final Supplier entity = supplierMapper.dtoToEntity(request);
        entity.setId(id);
        final Supplier updatedEntity = supplierRepository.save(entity);
        return supplierMapper.entityToDto(updatedEntity);
    }


    @Override
    public PageResponse<SupplierResponse> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public SupplierResponse findById(String s) {
        return null;
    }

    @Override
    public void delete(String s) {

    }
}
