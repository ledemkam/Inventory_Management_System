package com.kte.backend.mapper;

import com.kte.backend.models.dto.request.TransactionRequest;
import com.kte.backend.models.dto.response.TransactionResponse;
import com.kte.backend.models.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransactionMapper {
    /**
     * convert entity to dto
     *
     * @param entity : entity converting
     * @return dto corresponding
     */
    TransactionResponse entityToDto(Transaction entity);


    /**
     * convert dto to entity
     *
     * @param dto : dto converting
     * @return entity corresponding
     */
    Transaction dtoToEntity(TransactionRequest dto);

    /**
     * convert list of entity to list of dto
     *
     * @param entities : list of entities converting
     * @return list of corresponding dto
     */
    List<TransactionResponse> toDtoList(List<Transaction> entities);

    /**
     * Updates an existing entity with data from a DTO.
     *
     * @param dto    the source DTO
     * @param entity the entity to update
     */
    void updateEntityFromDto(TransactionRequest dto, @MappingTarget Transaction entity);
}