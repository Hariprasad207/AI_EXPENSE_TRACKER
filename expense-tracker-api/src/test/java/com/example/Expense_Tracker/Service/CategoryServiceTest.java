package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Exception.BadRequestException;
import com.example.Expense_Tracker.Exception.ResourceNotFoundException;
import com.example.Expense_Tracker.Repository.BudgetRepo;
import com.example.Expense_Tracker.Repository.CategoryRepo;
import com.example.Expense_Tracker.Repository.ExpenseRepo;
import com.example.Expense_Tracker.Repository.IncomeRepo;
import com.example.Expense_Tracker.Repository.NotificationRepo;
import com.example.Expense_Tracker.dto.Category.CategoryRequest;
import com.example.Expense_Tracker.dto.Category.CategoryResponse;
import com.example.Expense_Tracker.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long OTHER_USER_ID = 9L;
    private static final Long CATEGORY_ID = 20L;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ExpenseRepo expenseRepo;

    @Mock
    private IncomeRepo incomeRepo;

    @Mock
    private BudgetRepo budgetRepo;

    @Mock
    private NotificationRepo notificationRepo;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        when(currentUserService.getCurrentUserID()).thenReturn(USER_ID);
    }

    @Test
    void getAllCategories_returnsMappedCategories() {
        Category category = category(
                CATEGORY_ID, USER_ID, "Food",
                Category.CategoryType.EXPENSE, "food", true
        );

        when(categoryRepo.findAvailableCategories(USER_ID))
                .thenReturn(List.of(category));

        List<CategoryResponse> result =
                categoryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals(CATEGORY_ID, result.get(0).getId());
        assertEquals("Food", result.get(0).getName());
        assertEquals(Category.CategoryType.EXPENSE, result.get(0).getType());
        assertEquals("food", result.get(0).getIcon());
        assertEquals(Boolean.TRUE, result.get(0).getIsCustom());

        verify(categoryRepo).findAvailableCategories(USER_ID);
    }

    @Test
    void getAllCategories_returnsEmptyListWhenNoCategoriesExist() {
        when(categoryRepo.findAvailableCategories(USER_ID))
                .thenReturn(List.of());

        List<CategoryResponse> result =
                categoryService.getAllCategories();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCategoryByType_returnsCategoriesOfRequestedType() {
        Category category = category(
                CATEGORY_ID, USER_ID, "Salary",
                Category.CategoryType.INCOME, "salary", true
        );

        when(categoryRepo.findAvailableCategoryByType(
                USER_ID, Category.CategoryType.INCOME
        )).thenReturn(List.of(category));

        List<CategoryResponse> result =
                categoryService.getCategoryByType(
                        Category.CategoryType.INCOME
                );

        assertEquals(1, result.size());
        assertEquals("Salary", result.get(0).getName());
        assertEquals(Category.CategoryType.INCOME, result.get(0).getType());

        verify(categoryRepo).findAvailableCategoryByType(
                USER_ID, Category.CategoryType.INCOME
        );
    }

    @Test
    void getCategoryByType_returnsEmptyListWhenNoMatchExists() {
        when(categoryRepo.findAvailableCategoryByType(
                USER_ID, Category.CategoryType.EXPENSE
        )).thenReturn(List.of());

        List<CategoryResponse> result =
                categoryService.getCategoryByType(
                        Category.CategoryType.EXPENSE
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void getCategoryById_returnsCategoryWhenFound() {
        Category category = category(
                CATEGORY_ID, USER_ID, "Travel",
                Category.CategoryType.EXPENSE, "plane", true
        );

        when(categoryRepo.findAvailableCategory(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.of(category));

        CategoryResponse result =
                categoryService.getCategoryById(CATEGORY_ID);

        assertEquals(CATEGORY_ID, result.getId());
        assertEquals("Travel", result.getName());
        assertEquals(Category.CategoryType.EXPENSE, result.getType());
    }

    @Test
    void getCategoryById_throwsWhenCategoryIsUnavailable() {
        when(categoryRepo.findAvailableCategory(CATEGORY_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(CATEGORY_ID)
        );
    }

    @Test
    void createCategory_savesCategoryForCurrentUser() {
        CategoryRequest request = request(
                "Salary", Category.CategoryType.INCOME, "money"
        );

        when(categoryRepo.save(any(Category.class)))
                .thenAnswer(invocation -> {
                    Category saved = invocation.getArgument(0);
                    saved.setId(CATEGORY_ID);
                    return saved;
                });

        CategoryResponse result =
                categoryService.createCategory(request);

        ArgumentCaptor<Category> captor =
                ArgumentCaptor.forClass(Category.class);

        verify(categoryRepo).save(captor.capture());

        Category saved = captor.getValue();

        assertEquals(USER_ID, saved.getUserId());
        assertEquals("Salary", saved.getName());
        assertEquals(Category.CategoryType.INCOME, saved.getType());
        assertEquals("money", saved.getIcon());
        assertEquals(Boolean.TRUE, saved.getIsCustom());

        assertEquals(CATEGORY_ID, result.getId());
        assertEquals("Salary", result.getName());
    }

    @Test
    void updateCategory_updatesCategoryOwnedByCurrentUser() {
        Category existing = category(
                CATEGORY_ID, USER_ID, "Old Name",
                Category.CategoryType.EXPENSE, "old", true
        );

        CategoryRequest request = request(
                "Groceries", Category.CategoryType.EXPENSE, "basket"
        );

        when(categoryRepo.findById(CATEGORY_ID))
                .thenReturn(Optional.of(existing));

        when(categoryRepo.save(existing)).thenReturn(existing);

        CategoryResponse result =
                categoryService.updateCategory(CATEGORY_ID, request);

        assertEquals("Groceries", existing.getName());
        assertEquals(Category.CategoryType.EXPENSE, existing.getType());
        assertEquals("basket", existing.getIcon());

        assertEquals(CATEGORY_ID, result.getId());
        assertEquals("Groceries", result.getName());

        verify(categoryRepo).save(existing);
    }

    @Test
    void updateCategory_throwsWhenCategoryDoesNotExist() {
        CategoryRequest request = request(
                "Groceries", Category.CategoryType.EXPENSE, "basket"
        );

        when(categoryRepo.findById(CATEGORY_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(CATEGORY_ID, request)
        );

        verify(categoryRepo, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_rejectsCategoryOwnedByAnotherUser() {
        Category otherUsersCategory = category(
                CATEGORY_ID, OTHER_USER_ID, "Private",
                Category.CategoryType.EXPENSE, "lock", true
        );

        CategoryRequest request = request(
                "Changed", Category.CategoryType.EXPENSE, "new"
        );

        when(categoryRepo.findById(CATEGORY_ID))
                .thenReturn(Optional.of(otherUsersCategory));

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(CATEGORY_ID, request)
        );

        assertEquals("Private", otherUsersCategory.getName());
        verify(categoryRepo, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_deletesUnusedCategory() {
        Category category = category(
                CATEGORY_ID, USER_ID, "Unused",
                Category.CategoryType.EXPENSE, "unused", true
        );

        when(categoryRepo.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        when(expenseRepo.existsByCategory_Id(CATEGORY_ID))
                .thenReturn(false);
        when(incomeRepo.existsByCategoryId(CATEGORY_ID))
                .thenReturn(false);
        when(budgetRepo.existsByCategoryId(CATEGORY_ID))
                .thenReturn(false);
        when(notificationRepo.existsByCategoryId(CATEGORY_ID))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> categoryService.deleteCategory(CATEGORY_ID)
        );

        verify(categoryRepo).deleteById(CATEGORY_ID);
    }

    @Test
    void deleteCategory_throwsWhenCategoryDoesNotExist() {
        when(categoryRepo.findById(CATEGORY_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(CATEGORY_ID)
        );

        verify(categoryRepo, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_rejectsCategoryOwnedByAnotherUser() {
        Category category = category(
                CATEGORY_ID, OTHER_USER_ID, "Other User",
                Category.CategoryType.EXPENSE, "lock", true
        );

        when(categoryRepo.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(CATEGORY_ID)
        );

        verifyNoInteractions(
                expenseRepo, incomeRepo, budgetRepo, notificationRepo
        );
        verify(categoryRepo, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_rejectsCategoryReferencedByExpense() {
        givenCategoryExists();

        when(expenseRepo.existsByCategory_Id(CATEGORY_ID))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> categoryService.deleteCategory(CATEGORY_ID)
        );

        verify(categoryRepo, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_rejectsCategoryReferencedByIncome() {
        givenCategoryExists();

        when(expenseRepo.existsByCategory_Id(CATEGORY_ID))
                .thenReturn(false);
        when(incomeRepo.existsByCategoryId(CATEGORY_ID))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> categoryService.deleteCategory(CATEGORY_ID)
        );

        verify(categoryRepo, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_rejectsCategoryReferencedByBudget() {
        givenCategoryExists();

        when(expenseRepo.existsByCategory_Id(CATEGORY_ID))
                .thenReturn(false);
        when(incomeRepo.existsByCategoryId(CATEGORY_ID))
                .thenReturn(false);
        when(budgetRepo.existsByCategoryId(CATEGORY_ID))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> categoryService.deleteCategory(CATEGORY_ID)
        );

        verify(categoryRepo, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_rejectsCategoryReferencedByNotification() {
        givenCategoryExists();

        when(expenseRepo.existsByCategory_Id(CATEGORY_ID))
                .thenReturn(false);
        when(incomeRepo.existsByCategoryId(CATEGORY_ID))
                .thenReturn(false);
        when(budgetRepo.existsByCategoryId(CATEGORY_ID))
                .thenReturn(false);
        when(notificationRepo.existsByCategoryId(CATEGORY_ID))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> categoryService.deleteCategory(CATEGORY_ID)
        );

        verify(categoryRepo, never()).deleteById(anyLong());
    }

    private void givenCategoryExists() {
        Category category = category(
                CATEGORY_ID, USER_ID, "Food",
                Category.CategoryType.EXPENSE, "food", true
        );

        when(categoryRepo.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));
    }

    private Category category(
            Long id,
            Long userId,
            String name,
            Category.CategoryType type,
            String icon,
            Boolean isCustom
    ) {
        return Category.builder()
                .id(id)
                .userId(userId)
                .name(name)
                .type(type)
                .icon(icon)
                .isCustom(isCustom)
                .build();
    }

    private CategoryRequest request(
            String name,
            Category.CategoryType type,
            String icon
    ) {
        return CategoryRequest.builder()
                .name(name)
                .type(type)
                .icon(icon)
                .build();
    }
}
