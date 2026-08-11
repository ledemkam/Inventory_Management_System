package com.kte.backend.mapper;

import com.kte.backend.common.PageResponse;
import com.kte.backend.models.dto.request.CategoryRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.models.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {
    /**
     * convert entity to dto
     *
     * @param entity : entity converting
     * @return dto corresponding
     */
    CategoryResponse entityToDto(Category entity);


    /**
     * convert dto to entity
     *
     * @param dto : dto converting
     * @return entity corresponding
     */
    Category dtoToEntity(CategoryRequest dto);

    /**
     * convert list of entity to list of dto
     *
     * @param entities : list of entities converting
     * @return list of corresponding dto
     */
    List<CategoryResponse> toDtoList(List<Category> entities);

    /**
     * Updates an existing entity with data from a DTO.
     *
     * @param dto    the source DTO
     * @param entity the entity to update
     */
    void updateEntityFromDto(CategoryRequest dto, @MappingTarget Category entity);
}