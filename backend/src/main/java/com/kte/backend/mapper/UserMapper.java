package com.kte.backend.mapper;

import com.kte.backend.models.dto.request.RegisterRequest;
import com.kte.backend.models.dto.response.UserResponse;
import com.kte.backend.models.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    /**
     * convert entity to dto
     *
     * @param entity : entity converting
     * @return dto corresponding
     */
    UserResponse entityToDto(User entity);


    /**
     * convert dto to entity
     *
     * @param dto : dto converting
     * @return entity corresponding
     */
    User dtoToEntity(RegisterRequest dto);

    /**
     * convert list of entity to list of dto
     *
     * @param entities : list of entities converting
     * @return list of corresponding dto
     */
    List<UserResponse> toDtoList(List<User> entities);

    /**
     * Updates an existing entity with data from a DTO.
     *
     * @param dto    the source DTO
     * @param entity the entity to update
     */
    void updateEntityFromDto(RegisterRequest dto, @MappingTarget User entity);
}