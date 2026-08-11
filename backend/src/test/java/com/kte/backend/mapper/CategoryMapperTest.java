package com.kte.backend.mapper;

import com.kte.backend.models.dto.request.CategoryRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.models.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("CategoryMapper Test")
class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;


    @Test
    @DisplayName("Test entity to DTO mapping")
    void entity_To_Dto() {
        //Given
        Category category = Category.builder()
                .id("1L")
                .name("Electronics")
                .build();
        //When
        CategoryResponse dto = categoryMapper.entityToDto(category);
        //Then
        assertNotNull(dto);

    }

    @Test
    @DisplayName("Test DTO to entity mapping")
    void dto_To_Entity() {
        //Given
        CategoryRequest categoryRequest = CategoryRequest.builder()
                .name("Electronics")
                .build();
        //When
        Category entity = categoryMapper.dtoToEntity(categoryRequest);
        //Then
        assertNotNull(entity);
        assertEquals(categoryRequest.name(), entity.getName());
    }

    @Test
    @DisplayName("Test list of entities to list of DTOs mapping")
    void to_Dto_List() {
        //Given
        Category category1 = Category.builder()
                .name("Electronics")
                .build();

        Category category2 = Category.builder()
                .name("Clothing")
                .build();

        //When
        List<CategoryResponse> dtoList = categoryMapper.toDtoList(List.of(category1, category2));

        //Then
        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());
        assertEquals("Electronics", dtoList.get(0).name());
        assertEquals("Clothing", dtoList.get(1).name());
    }

    @Test
    @DisplayName("Test updating entity from DTO")
    void update_Entity_From_Dto() {
        //Given
        Category category = Category.builder()
                .id("1L")
                .name("Electronics")
                .build();

        CategoryRequest categoryRequest = CategoryRequest.builder()
                .name("Clothing")
                .build();

        //When
        categoryMapper.updateEntityFromDto(categoryRequest, category);

        //Then
        assertEquals("Clothing", category.getName());
        assertEquals("1L", category.getId());
    }
}