package com.kte.backend.catalog.services.impl;

import com.kte.backend.common.PageResponse;
import com.kte.backend.catalog.mapper.SupplierMapper;
import com.kte.backend.catalog.dto.request.SupplierRequest;
import com.kte.backend.catalog.dto.response.SupplierResponse;
import com.kte.backend.catalog.Supplier;
import com.kte.backend.catalog.repository.SupplierRepository;
import com.kte.backend.catalog.services.SupplierService;
import com.kte.backend.catalog.validator.SupplierValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public PageResponse<SupplierResponse> findAll(final Pageable pageable) {
        log.debug("Fetching all suppliers with pagination: page {}, size {}", pageable.getPageNumber(),
                pageable.getPageSize());
        return PageResponse.of(supplierRepository.findAll(pageable).map(supplierMapper::entityToDto));
    }

    @Override
    public SupplierResponse findById(final String id) {
        log.debug("Fetching supplier with id: {}", id);
        final Supplier entity = supplierValidator.findSupplierOrThrow(id);
        return supplierMapper.entityToDto(entity);
    }

    @Override
    public void delete(final String id) {
        log.info("Deleting supplier with id: {}", id);
        final Supplier entity = supplierValidator.findSupplierOrThrow(id);
        supplierRepository.delete(entity);

    }
}
