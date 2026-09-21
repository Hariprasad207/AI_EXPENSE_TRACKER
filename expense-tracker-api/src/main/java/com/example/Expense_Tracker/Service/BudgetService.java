package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.BudgetRepo;
import com.example.Expense_Tracker.Repository.CategoryRepo;
import com.example.Expense_Tracker.Repository.ExpenseRepo;
import com.example.Expense_Tracker.dto.Budget.BudgetProgressResponse;
import com.example.Expense_Tracker.dto.Budget.BudgetRequest;
import com.example.Expense_Tracker.dto.Budget.BudgetResponse;
import com.example.Expense_Tracker.entity.Budget;
import com.example.Expense_Tracker.entity.Category;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {

    private final BudgetRepo budgetRepo;
    private final CategoryRepo categoryRepo;
    private final ExpenseRepo expenseRepo;

    private void validateCategory(Long userId,Long categoryId) {
        if (categoryId == null) {
            return;
        }

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() ->new RuntimeException("This category is not found")
                );

        if (category.getType() != Category.CategoryType.EXPENSE) {
            throw new RuntimeException("Budget can only be created for expense category");
        }

        if (category.getUserId() != null && !category.getUserId().equals(userId)) {
            throw new RuntimeException("You do not have access to this category");
        }
    }


    private void validateDuplicateBudget(Long userId,Long categoryId,Integer month,Integer year) {
        List<Budget> existingBudgets =budgetRepo.findByUserIdAndMonthAndYear(userId,month,year);
        boolean duplicate = existingBudgets.stream()
                .anyMatch(existing ->
                        Objects.equals(existing.getCategoryId(),categoryId)
                );

        if (duplicate) {
            if (categoryId == null) {
                throw new RuntimeException("An overall budget already exists for this month");
            }

            throw new RuntimeException("A budget already exists for this category and month");
        }
    }

    private BudgetResponse mapToResponse(Budget budget) {
        String categoryName = null;
        if (budget.getCategoryId() != null) {
            categoryName = categoryRepo.findById(budget.getCategoryId())
                    .map(Category::getName)
                    .orElse(null);
        }

        return BudgetResponse.builder()
                .id(budget.getId())
                .userId(budget.getUserId())
                .categoryId(budget.getCategoryId())
                .categoryName(categoryName)
                .amount(budget.getAmount())
                .month(budget.getMonth())
                .year(budget.getYear())
                .build();
    }

    public BudgetResponse createBudget(Long userId,BudgetRequest request) {
        validateCategory(userId,request.getCategoryId());
        validateDuplicateBudget(userId,request.getCategoryId(),request.getMonth(),request.getYear());
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(request.getCategoryId())
                .amount(request.getAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .build();

        Budget savedBudget =budgetRepo.save(budget);
        return mapToResponse(savedBudget);
    }


    public List<BudgetResponse> getBudgets(Long userId) {
        List<Budget> budgets =budgetRepo.findByUserId(userId);
        return budgets.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BudgetResponse> getBudgets(Long userId,Integer month,Integer year) {
        List<Budget> budgets =budgetRepo.findByUserIdAndMonthAndYear(userId,month,year);

        return budgets.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BudgetResponse getBudgetById(Long userId,Long budgetId) {
        Budget budget =budgetRepo.findByIdAndUserId(budgetId,userId)
                        .orElseThrow(() ->new RuntimeException("Budget not found")
                        );

        return mapToResponse(budget);
    }

    public BudgetResponse updateBudgetById(Long userId,Long id,BudgetRequest request) {

        Budget budget =budgetRepo.findByIdAndUserId(id,userId)
                        .orElseThrow(() ->new RuntimeException("The budget is not found"));

        validateCategory(userId,request.getCategoryId());

        boolean categoryChanged =!Objects.equals(budget.getCategoryId(),request.getCategoryId());
        boolean monthChanged =!Objects.equals(budget.getMonth(),request.getMonth());
        boolean yearChanged =!Objects.equals(budget.getYear(),request.getYear());

        if (categoryChanged || monthChanged ||yearChanged) {
            validateDuplicateBudget(userId,request.getCategoryId(),request.getMonth(),request.getYear());
        }

        budget.setCategoryId(request.getCategoryId());
        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());

        Budget updatedBudget =budgetRepo.save(budget);
        return mapToResponse(updatedBudget);
    }

    public void deleteBudget(Long id,Long userId) {
        Budget budget =budgetRepo.findByIdAndUserId(id,userId)
                        .orElseThrow(() -> new RuntimeException("The budget is not found"));

        budgetRepo.delete(budget);
    }

    public BudgetProgressResponse getBudgetProgress(Long userId, Long budgetId) {

        Budget budget = budgetRepo.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() ->
                        new RuntimeException("That budget is not found")
                );

        LocalDate startDate = LocalDate.of(
                budget.getYear(),
                budget.getMonth(),
                1
        );
        LocalDate endDate = startDate.plusMonths(1);
        BigDecimal spentAmount;



        if (budget.getCategoryId() != null) {
            spentAmount =
                    expenseRepo.getTotalExpenseForCategoryAndDateRange(
                            userId,
                            budget.getCategoryId(),
                            startDate,
                            endDate
                    );
        }

        else {
            spentAmount =expenseRepo.getTotalExpenseForDateRange(
                            userId,
                            startDate,
                            endDate
                    );
        }

        BigDecimal budgetAmount = budget.getAmount();
        BigDecimal remainingAmount =budgetAmount.subtract(spentAmount);
        BigDecimal percentageUsed;

        if (budgetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            percentageUsed = BigDecimal.ZERO;
        } else {
            percentageUsed = spentAmount
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                            budgetAmount,
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        boolean exceed =spentAmount.compareTo(budgetAmount) > 0;
        String categoryName = null;
        if (budget.getCategoryId() != null) {
            categoryName =
                    categoryRepo.findById(budget.getCategoryId())
                            .map(Category::getName)
                            .orElse(null);
        }

        return BudgetProgressResponse.builder()
                .budgetId(budget.getId())
                .categoryId(budget.getCategoryId())
                .categoryName(categoryName)
                .budgetAmount(budgetAmount)
                .spentAmount(spentAmount)
                .remainingAmount(remainingAmount)
                .percentageUsed(percentageUsed)
                .exceed(exceed)
                .month(budget.getMonth())
                .year(budget.getYear())
                .build();
    }
}