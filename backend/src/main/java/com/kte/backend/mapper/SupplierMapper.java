package com.kte.backend.mapper;

import com.kte.backend.models.dto.request.SupplierRequest;
import com.kte.backend.models.dto.response.SupplierResponse;
import com.kte.backend.models.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplierMapper {
    /**
     * convert entity to dto
     *
     * @param entity : entity converting
     * @return dto corresponding
     */
    SupplierResponse entityToDto(Supplier entity);


    /**
     * convert dto to entity
     *
     * @param dto : dto converting
     * @return entity corresponding
     */
    Supplier dtoToEntity(SupplierRequest dto);

    /**
     * convert list of entity to list of dto
     *
     * @param entities : list of entities converting
     * @return list of corresponding dto
     */
    List<SupplierResponse> toDtoList(List<Supplier> entities);

    /**
     * Updates an existing entity with data from a DTO.
     *
     * @param dto    the source DTO
     * @param entity the entity to update
     */
    void updateEntityFromDto(SupplierRequest dto, @MappingTarget Supplier entity);
}