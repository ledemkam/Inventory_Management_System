package com.kte.backend.repository;

import com.kte.backend.models.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
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


    @Test
    @DisplayName("Find all categories returns all saved categories")
    void find_All_Categories() {
        //Given
        Category category1 = Category.builder()
                .name("Electronics")
                .build();
        Category category2 = Category.builder()
                .name("Books")
                .build();
        categoryRepository.save(category1);
        categoryRepository.save(category2);

        //When
        Iterable<Category> categories = categoryRepository.findAll();

        //Then
        assertThat(categories).isNotEmpty();
    }

    @Test
    @DisplayName("Save category with success")
    void save_Category_With_Success() {
        //Given
        Category category = Category.builder()
                .name("Electronics")
                .build();

        //When
        Category savedCategory = categoryRepository.save(category);

        //Then
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isNotNull();
    }

    @Test
    @DisplayName("Find category by id")
    void findById() {
        //Given
        Category category = Category.builder()
                .name("Electronics")
                .build();
        Category savedCategory = categoryRepository.save(category);

        //When
        Optional<Category> foundCategory = categoryRepository.findById(savedCategory.getId());

        //Then
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getId()).isEqualTo(savedCategory.getId());
    }

    @Test
    @DisplayName("Delete category by id")
    void deleteById() {
        //Given
        Category category = Category.builder()
                .name("Electronics")
                .build();
        Category savedCategory = categoryRepository.save(category);

        //When
        categoryRepository.deleteById(savedCategory.getId());

        //Then
        Optional<Category> deletedCategory = categoryRepository.findById(savedCategory.getId());
        assertThat(deletedCategory).isEmpty();
    }
}