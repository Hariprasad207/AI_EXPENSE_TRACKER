package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.ResourceNotFoundException;
import com.example.Expense_Tracker.Repository.BudgetRepo;
import com.example.Expense_Tracker.Repository.CategoryRepo;
import com.example.Expense_Tracker.Repository.ExpenseRepo;
import com.example.Expense_Tracker.dto.Budget.BudgetRequest;
import com.example.Expense_Tracker.entity.Budget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceExceptionTest {

    @Mock
    private BudgetRepo budgetRepo;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private ExpenseRepo expenseRepo;

    @InjectMocks
    private BudgetService budgetService;

    private final Long userId = 1L;
    private final Long budgetId = 100L;

    @Test
    void getBudgetById_shouldThrowResourceNotFoundWhenMissing() {
        when(budgetRepo.findByIdAndUserId(budgetId, userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> budgetService.getBudgetById(userId, budgetId)
        );

        assertEquals("Budget not found", exception.getMessage());
    }

    @Test
    void updateBudgetById_shouldThrowResourceNotFoundWhenMissing() {
        when(budgetRepo.findByIdAndUserId(budgetId, userId))
                .thenReturn(Optional.empty());

        BudgetRequest request = mock(BudgetRequest.class);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> budgetService.updateBudgetById(userId, budgetId, request)
        );

        assertEquals("The budget is not found", exception.getMessage());
        verify(budgetRepo, never()).save(any(Budget.class));
    }

    @Test
    void deleteBudget_shouldThrowResourceNotFoundWhenMissing() {
        when(budgetRepo.findByIdAndUserId(budgetId, userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> budgetService.deleteBudget(budgetId, userId)
        );

        assertEquals("The budget is not found", exception.getMessage());
        verify(budgetRepo, never()).delete(any(Budget.class));
    }

    @Test
    void getBudgetProgress_shouldThrowResourceNotFoundWhenMissing() {
        when(budgetRepo.findByIdAndUserId(budgetId, userId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> budgetService.getBudgetProgress(userId, budgetId)
        );

        assertEquals("That budget is not found", exception.getMessage());
        verifyNoInteractions(expenseRepo);
    }

    @Test
    void createBudget_shouldThrowResourceNotFoundWhenCategoryIsMissing() {
        Long categoryId = 10L;
        BudgetRequest request = mock(BudgetRequest.class);

        when(request.getCategoryId()).thenReturn(categoryId);
        when(categoryRepo.findById(categoryId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> budgetService.createBudget(userId, request)
        );

        assertEquals("This category is not found", exception.getMessage());
        verify(budgetRepo, never()).save(any(Budget.class));
    }
}