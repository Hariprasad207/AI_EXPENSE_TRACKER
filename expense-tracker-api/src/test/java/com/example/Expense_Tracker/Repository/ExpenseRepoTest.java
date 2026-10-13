
package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Category;
import com.example.Expense_Tracker.entity.Expense;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepoTest {

    @Autowired
    private ExpenseRepo expenseRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private EntityManager entityManager;

    private Category groceriesCategory;
    private Category otherUserCategory;

    private final Long userId = 101L;
    private final Long otherUserId = 202L;

    @BeforeEach
    void setUp() {
        groceriesCategory = categoryRepo.saveAndFlush(
                Category.builder()
                        .userId(userId)
                        .name("Groceries")
                        .type(Category.CategoryType.EXPENSE)
                        .icon("shopping-cart")
                        .isCustom(true)
                        .build()
        );

        otherUserCategory = categoryRepo.saveAndFlush(
                Category.builder()
                        .userId(otherUserId)
                        .name("Travel")
                        .type(Category.CategoryType.EXPENSE)
                        .icon("car")
                        .isCustom(true)
                        .build()
        );

        saveExpense(userId, groceriesCategory, "120.00",
                Expense.PaymentMode.UPI, LocalDate.of(2026, 1, 10),
                "Groceries shopping");

        saveExpense(userId, groceriesCategory, "80.00",
                Expense.PaymentMode.CASH, LocalDate.of(2026, 1, 20),
                "Vegetables");

        saveExpense(userId, groceriesCategory, "50.00",
                Expense.PaymentMode.UPI, LocalDate.of(2026, 2, 1),
                "Monthly supplies");

        saveExpense(otherUserId, otherUserCategory, "1000.00",
                Expense.PaymentMode.UPI, LocalDate.of(2026, 1, 15),
                "Other user's expense");

        entityManager.flush();
        entityManager.clear();
    }

    private Expense saveExpense(
            Long expenseUserId,
            Category category,
            String amount,
            Expense.PaymentMode paymentMode,
            LocalDate date,
            String title
    ) {
        return expenseRepo.save(
                Expense.builder()
                        .userId(expenseUserId)
                        .category(category)
                        .amount(new BigDecimal(amount))
                        .paymentMode(paymentMode)
                        .expenseDate(date)
                        .title(title)
                        .description("Repository test data")
                        .build()
        );
    }

    @Test
    void shouldSaveExpenseAndGenerateId() {
        Expense expense = saveExpense(
                userId,
                groceriesCategory,
                "45.00",
                Expense.PaymentMode.CASH,
                LocalDate.of(2026, 3, 1),
                "Test expense"
        );

        expenseRepo.flush();

        assertThat(expense.getId()).isNotNull();
        assertThat(expenseRepo.findById(expense.getId())).isPresent();
    }

    @Test
    void shouldFindExpenseByIdAndUserId() {
        Expense expense = expenseRepo.findAll().stream()
                .filter(e -> e.getUserId().equals(userId))
                .findFirst()
                .orElseThrow();

        assertThat(
                expenseRepo.findByIdAndUserId(expense.getId(), userId)
        ).isPresent();
    }

    @Test
    void shouldNotReturnExpenseForAnotherUser() {
        Expense expense = expenseRepo.findAll().stream()
                .filter(e -> e.getUserId().equals(userId))
                .findFirst()
                .orElseThrow();

        assertThat(
                expenseRepo.findByIdAndUserId(expense.getId(), otherUserId)
        ).isEmpty();
    }

    @Test
    void shouldCalculateCategoryExpenseForDateRange() {
        BigDecimal total =
                expenseRepo.getTotalExpenseForCategoryAndDateRange(
                        userId,
                        groceriesCategory.getId(),
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 2, 1)
                );

        assertThat(total).isEqualByComparingTo("200.00");
    }

    @Test
    void shouldCalculateTotalExpenseForDateRangeAndUser() {
        BigDecimal total = expenseRepo.getTotalExpenseForDateRange(
                userId,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1)
        );

        assertThat(total).isEqualByComparingTo("200.00");
    }

    @Test
    void shouldCountExpensesForUserOnDate() {
        long count = expenseRepo.countExpensesForUserOnDate(
                userId,
                LocalDate.of(2026, 1, 10)
        );

        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldFindExpenseWithCategoryForCorrectUser() {
        Expense expense = expenseRepo.findAll().stream()
                .filter(e -> e.getUserId().equals(userId))
                .findFirst()
                .orElseThrow();

        var result = expenseRepo.findByIdAndUserIdWithCategory(
                expense.getId(), userId
        );

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getCategory().getName())
                .isEqualTo("Groceries");
    }

    @Test
    void shouldFilterExpensesByCategoryPaymentModeAndDate() {
        Page<Expense> result = expenseRepo.findExpensesWithFilters(
                userId,
                groceriesCategory.getId(),
                Expense.PaymentMode.UPI,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle())
                .isEqualTo("Groceries shopping");
    }

    @Test
    void shouldPaginateExpensesForUser() {
        Page<Expense> result = expenseRepo.findExpensesWithFilters(
                userId,
                null,
                null,
                null,
                null,
                PageRequest.of(
                        0, 1,
                        Sort.by(Sort.Direction.ASC, "expenseDate")
                )
        );

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getExpenseDate())
                .isEqualTo(LocalDate.of(2026, 1, 10));
    }
}
