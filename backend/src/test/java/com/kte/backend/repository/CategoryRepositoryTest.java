package com.kte.backend.repository;

import com.kte.backend.models.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
@DisplayName("CategoryRepository Test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;


    @Test
    @DisplayName("Find category by name ignoring case")
    void findByNameIgnoreCase_ReturnsCategory_WhenNameMatchesIgnoringCase() {
        //Given
        Category category = categoryRepository.save(
                Category.builder()
                        .name("Electronics")
                        .build()
        );

        //When
        Optional<Category> found = categoryRepository.findByNameIgnoreCase("electronics");

        //Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("Return empty when no category matches the given name")
    void findByNameIgnoreCase_ReturnsEmpty_WhenNoCategoryMatches() {
        //When
        Optional<Category> found = categoryRepository.findByNameIgnoreCase("Nonexistent");

        //Then
        assertThat(found).isEmpty();
    }

}