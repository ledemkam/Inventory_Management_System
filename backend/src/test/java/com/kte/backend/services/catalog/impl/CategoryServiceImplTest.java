package com.kte.backend.services.catalog.impl;

import com.kte.backend.Validator.CategoryValidator;
import com.kte.backend.common.PageResponse;
import com.kte.backend.mapper.CategoryMapper;
import com.kte.backend.models.dto.request.CategoryRequest;
import com.kte.backend.models.dto.response.CategoryResponse;
import com.kte.backend.models.entity.Category;
import com.kte.backend.repository.CategoryRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@DisplayName("CategoryServiceImpl Unit Tests")
class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;


    @Mock
    private CategoryValidator categoryValidator;

    private Category category;
    private CategoryResponse categoryResponse;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id("1")
                .name("Electronics")
                .products(List.of())
                .build();
        categoryResponse = CategoryResponse.builder()
                .id("1")
                .name("Electronics")
                .build();
        categoryRequest = CategoryRequest.builder()
                .name("Electronics")
                .build();
    }

    @Test
    @DisplayName("Test create method")
    void should_create_category_when_no_exist() {
        //GIVEN
        doNothing().when(categoryValidator).checkCategoryAlreadyExistsByName(categoryRequest.name());
        when(categoryMapper.dtoToEntity(any(CategoryRequest.class))).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.entityToDto(any(Category.class))).thenReturn(categoryResponse);

        //WHEN
        CategoryResponse response = categoryService.create(categoryRequest);

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(categoryResponse.id());
        assertThat(response.name()).isEqualTo(categoryResponse.name());
    }

    @Test
    @DisplayName("Test update method")
    void should_update_category_when_exist() {
        //GIVEN
        when(categoryValidator.findCategoryOrThrow(category.getId())).thenReturn(category);
        doNothing().when(categoryMapper).updateEntityFromDto(categoryRequest, category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.entityToDto(any(Category.class))).thenReturn(categoryResponse);

        //WHEN
        CategoryResponse response = categoryService.update(category.getId(), categoryRequest);

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(categoryResponse.id());
        assertThat(response.name()).isEqualTo(categoryResponse.name());
    }

    @Test
    @DisplayName("Test findAll method")
    void should_find_All_categories() {
        //GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> categoryPage = new PageImpl<>(List.of(category), pageable, 1);
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.entityToDto(category)).thenReturn(categoryResponse);

        //WHEN
        PageResponse<CategoryResponse> responsePage = categoryService.findAll(pageable);

        //THEN
        assertThat(responsePage).isNotNull()
                .hasFieldOrPropertyWithValue("page", 0)
                .hasFieldOrPropertyWithValue("size", 10)
                .hasFieldOrPropertyWithValue("totalElements", 1)
                .hasFieldOrPropertyWithValue("totalPages", 1)
                .hasFieldOrPropertyWithValue("hasNext", false)
                .hasFieldOrPropertyWithValue("hasPrevious", false)
                .hasFieldOrPropertyWithValue("isFirst", true)
                .hasFieldOrPropertyWithValue("isLast", true);

        assertThat(responsePage.getContent())
                .isNotNull()
                .hasSize(1)
                .containsExactly(categoryResponse);


    }

    @Test
    @DisplayName("Test findById method")
    void should_find_category_By_Id() {
        //GIVEN
        when(categoryValidator.findCategoryOrThrow(category.getId())).thenReturn(category);
        when(categoryMapper.entityToDto(category)).thenReturn(categoryResponse);

        //WHEN
        CategoryResponse response = categoryService.findById(category.getId());

        //THEN
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(categoryResponse.id());
        assertThat(response.name()).isEqualTo(categoryResponse.name());
    }

    @Test
    @DisplayName("Test delete method")
    void should_delete_category_by_id() {
        //GIVEN
        when(categoryValidator.findCategoryOrThrow(category.getId())).thenReturn(category);
        doNothing().when(categoryRepository).delete(category);

        //WHEN
        categoryService.delete(category.getId());

        //THEN
        assertThat(categoryService).isNotNull();
    }


}