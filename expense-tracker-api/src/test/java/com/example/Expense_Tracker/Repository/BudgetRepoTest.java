package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Budget;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class BudgetRepoTest {
    @Autowired
    private BudgetRepo budgetRepo;
    @Autowired
    private EntityManager entityManager;

    private final Long userId = 101L;
    private final Long otherUserId = 202L;
    private Budget overallBudget;
    private Budget foodBudget;
    private Budget travelBudget;
    private Budget otherUserBudget;

    @BeforeEach void setup(){
        overallBudget = budgetRepo.saveAndFlush(
                Budget.builder()
                        .userId(userId)
                        .categoryId(null)
                        .amount(new BigDecimal("15000.00"))
                        .month(1)
                        .year(2026)
                        .build()
        );
        foodBudget = budgetRepo.saveAndFlush(
                Budget.builder()
                        .userId(userId)
                        .categoryId(10L)
                        .amount(new BigDecimal("5000.00"))
                        .month(1)
                        .year(2026)
                        .build()
        );
        travelBudget = budgetRepo.saveAndFlush(
                Budget.builder()
                        .userId(userId)
                        .categoryId(20L)
                        .amount(new BigDecimal("3000.00"))
                        .month(2)
                        .year(2026)
                        .build()
        );

        otherUserBudget = budgetRepo.saveAndFlush(
                Budget.builder()
                        .userId(otherUserId)
                        .categoryId(10L)
                        .amount(new BigDecimal("8000.00"))
                        .month(1)
                        .year(2026)
                        .build()
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Test void shouldSaveBudgetAndGenerateID(){
        Budget saved = budgetRepo.saveAndFlush(
                Budget.builder()
                        .userId(userId)
                        .categoryId(30L)
                        .amount(new BigDecimal("2500.00"))
                        .month(3)
                        .year(2026)
                        .build()
        );

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo("2500.00");
    }

    @Test void shouldFindBudgetByIdAndUserId(){
        Optional<Budget> result = budgetRepo.findByIdAndUserId(foodBudget.getId(),userId);

        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(10L);
        assertThat(result.get().getAmount()).isEqualByComparingTo("5000.00");
    }

    @Test void shouldNotFindAnotherUsersBudget(){
        Optional<Budget> result = budgetRepo.findByIdAndUserId(otherUserBudget.getId(),userId);
        assertThat(result).isEmpty();
    }

    @Test void shouldReturnAllBudgetsForUser(){
        List<Budget> budgets = budgetRepo.findByUserId(userId);

        assertThat(budgets).hasSize(3);
        assertThat(budgets).allMatch(
                budget -> budget.getUserId().equals(userId));
    }

    @Test void shouldFindBudgetsByUserMonthAndYear(){
        List<Budget> budgets =
                budgetRepo.findByUserIdAndMonthAndYear(
                        userId, 1, 2026);

        assertThat(budgets).hasSize(2);
        assertThat(budgets).allMatch(budget ->
                        budget.getUserId().equals(userId)
                                && budget.getMonth().equals(1)
                                && budget.getYear().equals(2026)
                );
    }

    @Test
    void shouldFindOverallBudgetForUserAndMonth() {
        Optional<Budget> result =budgetRepo.findByUserIdAndCategoryIdIsNullAndMonthAndYear(
                        userId, 1, 2026);

        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isNull();
        assertThat(result.get().getAmount())
                .isEqualByComparingTo("15000.00");
    }

    @Test
    void shouldFindCategorySpecificBudget() {
        Optional<Budget> result =budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(
                        userId, 10L, 1, 2026);

        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isEqualTo(10L);
        assertThat(result.get().getAmount())
                .isEqualByComparingTo("5000.00");
    }

    @Test
    void shouldReturnEmptyWhenCategoryBudgetDoesNotExist() {
        Optional<Budget> result =budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(
                        userId, 999L, 1, 2026);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenOverallBudgetDoesNotExistForMonth() {
        Optional<Budget> result =budgetRepo.findByUserIdAndCategoryIdIsNullAndMonthAndYear(
                        userId, 5, 2026);

        assertThat(result).isEmpty();
    }
}
