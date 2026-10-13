package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Category;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepoTest {

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private EntityManager entityManager;

    private Category sharedExpenseCategory;
    private Category sharedIncomeCategory;
    private Category userExpenseCategory;
    private Category otherUserExpenseCategory;

    private final Long userId = 101L;
    private final Long otherUserId = 202L;

    @BeforeEach
    void setUp() {

        sharedExpenseCategory = categoryRepo.saveAndFlush(
                Category.builder()
                        .userId(null)
                        .name("Food")
                        .type(Category.CategoryType.EXPENSE)
                        .icon("utensils")
                        .isCustom(false)
                        .build()
        );

        sharedIncomeCategory = categoryRepo.saveAndFlush(
                Category.builder()
                        .userId(null)
                        .name("Salary")
                        .type(Category.CategoryType.INCOME)
                        .icon("wallet")
                        .isCustom(false)
                        .build()
        );

        userExpenseCategory = categoryRepo.saveAndFlush(
                Category.builder()
                        .userId(userId)
                        .name("Personal Shopping")
                        .type(Category.CategoryType.EXPENSE)
                        .icon("shopping-bag")
                        .isCustom(true)
                        .build()
        );

        otherUserExpenseCategory = categoryRepo.saveAndFlush(
                Category.builder()
                        .userId(otherUserId)
                        .name("Travel")
                        .type(Category.CategoryType.EXPENSE)
                        .icon("car")
                        .isCustom(true)
                        .build()
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldSaveCategoryAndGenerateId() {

        Category savedCategory = categoryRepo.saveAndFlush(
                Category.builder()
                        .userId(userId)
                        .name("Entertainment")
                        .type(Category.CategoryType.EXPENSE)
                        .icon("film")
                        .isCustom(true)
                        .build()
        );

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Entertainment");
    }

    @Test
    void shouldFindSharedAndOwnCategoriesButNotOtherUsersCategories() {

        List<Category> categories =
                categoryRepo.findAvailableCategories(userId);

        assertThat(categories)
                .extracting(Category::getName)
                .contains("Food", "Salary", "Personal Shopping")
                .doesNotContain("Travel");
    }

    @Test
    void shouldFindOnlyAvailableExpenseCategories() {

        List<Category> categories =
                categoryRepo.findAvailableCategoryByType(
                        userId,
                        Category.CategoryType.EXPENSE
                );

        assertThat(categories)
                .extracting(Category::getName)
                .contains("Food", "Personal Shopping")
                .doesNotContain("Salary", "Travel");
    }

    @Test
    void shouldFindOnlyAvailableIncomeCategories() {

        List<Category> categories =
                categoryRepo.findAvailableCategoryByType(
                        userId,
                        Category.CategoryType.INCOME
                );

        assertThat(categories)
                .extracting(Category::getName)
                .containsExactly("Salary");
    }

    @Test
    void shouldAllowUserToFindSharedCategory() {

        Optional<Category> result =
                categoryRepo.findAvailableCategory(
                        sharedExpenseCategory.getId(),
                        userId
                );

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Food");
    }

    @Test
    void shouldAllowUserToFindOwnCategory() {

        Optional<Category> result =
                categoryRepo.findAvailableCategory(
                        userExpenseCategory.getId(),
                        userId
                );

        assertThat(result).isPresent();
        assertThat(result.get().getName())
                .isEqualTo("Personal Shopping");
    }

    @Test
    void shouldNotAllowUserToFindAnotherUsersPrivateCategory() {

        Optional<Category> result =
                categoryRepo.findAvailableCategory(
                        otherUserExpenseCategory.getId(),
                        userId
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenCategoryDoesNotExist() {

        Optional<Category> result =
                categoryRepo.findAvailableCategory(
                        999999L,
                        userId
                );

        assertThat(result).isEmpty();
    }
}
