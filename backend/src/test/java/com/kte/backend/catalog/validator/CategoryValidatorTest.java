package com.kte.backend.catalog.validator;

import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.catalog.Category;
import com.kte.backend.catalog.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryValidator Unit Tests")
class CategoryValidatorTest {

    @InjectMocks
    private CategoryValidator categoryValidator;

    @Mock
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id("1")
                .name("Electronics")
                .build();
    }

    @Test
    @DisplayName("checkCategoryAlreadyExistsByName: does nothing when no category with the name exists")
    void should_not_throw_when_category_name_is_free() {
        //GIVEN
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.empty());

        //WHEN / THEN
        assertThatCode(() -> categoryValidator.checkCategoryAlreadyExistsByName("Electronics"))
                .doesNotThrowAnyException();
        verify(categoryRepository).findByNameIgnoreCase("Electronics");
    }

    @Test
    @DisplayName("checkCategoryAlreadyExistsByName: throws EntityAlreadyExistsException when the name is taken")
    void should_throw_when_category_name_already_exists() {
        //GIVEN
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(category));

        //WHEN / THEN
        assertThatThrownBy(() -> categoryValidator.checkCategoryAlreadyExistsByName("Electronics"))
                .hasMessage("Electronics");
    }
}
