package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Repository.CategoryRepo;
import com.example.Expense_Tracker.Repository.ExpenseRepo;
import com.example.Expense_Tracker.dto.Expense.CreateExpenseRequest;
import com.example.Expense_Tracker.dto.Expense.ExpenseResponse;
import com.example.Expense_Tracker.dto.Expense.UpdateExpenseRequest;
import com.example.Expense_Tracker.entity.Category;
import com.example.Expense_Tracker.entity.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {
    @Mock
    private ExpenseRepo expenseRepo;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private BudgetAlertService budgetAlertService;

    @InjectMocks
    private ExpenseService expenseService;

    private Long userId;
    private Category category;
    private Expense expense;

    @BeforeEach
    void setup(){
        userId = 1L;

        category = Category.builder()
                .id(10L)
                .userId(null)
                .type(Category.CategoryType.EXPENSE)
                .icon("something")
                .isCustom(false)
                .name("Food")
                .build();

        expense = Expense.builder()
                .id(100L)
                .userId(userId)
                .category(category)
                .amount(new BigDecimal("250.00"))
                .paymentMode(Expense.PaymentMode.UPI)
                .expenseDate(LocalDate.of(2026, 10, 5))
                .title("Lunch")
                .description("Lunch at restaurant")
                .build();
    }

    @Test
    void getExpenseById_shouldReturnExpense(){
        when(currentUserService.getCurrentUserID())
                .thenReturn(userId);
        when(expenseRepo.findByIdAndUserIdWithCategory(100L,userId))
                .thenReturn(Optional.of(expense));

        ExpenseResponse response = expenseService.getExpenseById(100L);

        assertNotNull(response);
        assertEquals(100L,response.getId());
        assertEquals(10L,response.getCategoryId());
        assertEquals("Food",response.getCategoryName());
        assertEquals(new BigDecimal("250.00"),response.getAmount());
        assertEquals(Expense.PaymentMode.UPI,response.getPaymentMode());
        assertEquals("Lunch",response.getTitle());

        verify(expenseRepo)
                .findByIdAndUserIdWithCategory(100L,userId);
    }

    @Test
    void getExpenseById_ShouldThrowExceptionWhenExpenseDoesNotExist(){
        when(currentUserService.getCurrentUserID())
                .thenReturn(userId);
        when(expenseRepo.findByIdAndUserIdWithCategory(999L,userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,()-> expenseService.getExpenseById(999L)
        );

        assertEquals("Expense is not found",exception.getMessage());
        verify(expenseRepo)
                .findByIdAndUserIdWithCategory(999L,userId);
    }

    @Test
    void currentExpense_ShouldCreateExpense(){

        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setCategoryId(10L);
        request.setExpenseDate(LocalDate.of(2026,10,5));
        request.setTitle("Dinner");
        request.setAmount(BigDecimal.valueOf(500.00));
        request.setDescription("Great Dinner at thambi");

        when(currentUserService.getCurrentUserID())
                .thenReturn(userId);

        when(categoryRepo.findAvailableCategory(10L,userId))
                .thenReturn(Optional.of(category));

        when(expenseRepo.save(any(Expense.class)))
                .thenReturn(expense);

        ExpenseResponse response = expenseService.currentExpense(request);
        assertNotNull(response);
        assertEquals(100L,response.getId());
        assertEquals("Food",response.getCategoryName());

        verify(categoryRepo)
                .findAvailableCategory(10L,userId);
        verify(expenseRepo)
                .save(any(Expense.class));
        verify(budgetAlertService)
                .checkBudgetAlerts(userId,response.getCategoryId(),response.getExpenseDate());
    }

    @Test
    void currentExpense_ShouldThrowExceptionWhenCategoryNotExist(){
        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setCategoryId(100L);
        request.setExpenseDate(LocalDate.of(2026,10,5));
        request.setTitle("Dinner");
        request.setAmount(BigDecimal.valueOf(500.00));
        request.setDescription("Great Dinner at thambi");

        when(currentUserService.getCurrentUserID())
                .thenReturn(userId);

        when(categoryRepo.findAvailableCategory(100L,userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, ()-> expenseService.currentExpense(request)
        );

        assertEquals("Category not found",exception.getMessage());

        verify(expenseRepo,never())
                .save(any(Expense.class));
        verify(budgetAlertService,never())
                .checkBudgetAlerts(anyLong(),anyLong(),any());
    }

    @Test
    void updateExpense_shouldUpdateSuccessfully() {
        UpdateExpenseRequest request = new UpdateExpenseRequest();

        request.setCategoryId(10L);
        request.setAmount(new BigDecimal("750.00"));
        request.setPaymentMode(Expense.PaymentMode.CREDIT_CARD);
        request.setExpenseDate(LocalDate.of(2026, 10, 6));
        request.setTitle("Updated Dinner");
        request.setDescription("Updated description");

        when(currentUserService.getCurrentUserID())
                .thenReturn(userId);

        when(expenseRepo.findByIdAndUserIdWithCategory(100L, userId))
                .thenReturn(Optional.of(expense));

        when(categoryRepo.findAvailableCategory(10L, userId))
                .thenReturn(Optional.of(category));

        when(expenseRepo.save(expense))
                .thenReturn(expense);

        ExpenseResponse response = expenseService.updateExpense(100L, request);

        assertNotNull(response);

        assertEquals(new BigDecimal("750.00"), expense.getAmount());
        assertEquals(Expense.PaymentMode.CREDIT_CARD, expense.getPaymentMode());
        assertEquals(LocalDate.of(2026, 10, 6), expense.getExpenseDate());
        assertEquals("Updated Dinner", expense.getTitle());
        assertEquals("Updated description", expense.getDescription());

        verify(expenseRepo).save(expense);

        verify(budgetAlertService, times(2))
                .checkBudgetAlerts(anyLong(), anyLong(), any());
    }

    @Test
    void updateExpense_ShouldThrowExceptionWhenExpenseDoesNotExist() {
        UpdateExpenseRequest request = new UpdateExpenseRequest();

        when(currentUserService.getCurrentUserID())
                .thenReturn(userId);

        when(expenseRepo.findByIdAndUserIdWithCategory(190L, userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class, ()-> expenseService.updateExpense(190L,request)
        );

        assertEquals("Expense is not found",exception.getMessage());

        verify(expenseRepo,never())
                .save(any(Expense.class));
        verify(budgetAlertService,never())
                .checkBudgetAlerts(anyLong(),anyLong(),any());
    }

    @Test
    void updateExpense_shouldThrowExceptionWhenCategoryDoesNotExist() {
        UpdateExpenseRequest request = new UpdateExpenseRequest();

        request.setCategoryId(999L);
        request.setAmount(new BigDecimal("750.00"));
        request.setPaymentMode(Expense.PaymentMode.UPI);
        request.setExpenseDate(LocalDate.of(2026, 10, 6));
        request.setTitle("Updated Dinner");

        when(currentUserService.getCurrentUserID())
                .thenReturn(userId);

        when(expenseRepo.findByIdAndUserIdWithCategory(100L, userId))
                .thenReturn(Optional.of(expense));

        when(categoryRepo.findAvailableCategory(999L, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> expenseService.updateExpense(100L, request)
        );

        verify(expenseRepo, never())
                .save(any(Expense.class));

        verify(budgetAlertService, never())
                .checkBudgetAlerts(anyLong(), anyLong(), any());
    }

    @Test
    void deleteExpense_shouldDeleteSuccessfully() {
        when(currentUserService.getCurrentUserID())
                .thenReturn(userId);

        when(expenseRepo.findByIdAndUserId(100L, userId))
                .thenReturn(Optional.of(expense));

        expenseService.deleteExpense(100L);

        verify(expenseRepo)
                .delete(expense);

        verify(budgetAlertService)
                .checkBudgetAlerts(userId,category.getId(),expense.getExpenseDate());
    }

    @Test
    void deleteExpense_shouldThrowExceptionWhenExpenseDoesNotExist() {
        when(currentUserService.getCurrentUserID())
                .thenReturn(userId);

        when(expenseRepo.findByIdAndUserId(999L, userId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> expenseService.deleteExpense(999L)
        );

        assertEquals("Expense not found", exception.getMessage());

        verify(expenseRepo, never())
                .delete(any(Expense.class));

        verify(budgetAlertService, never())
                .checkBudgetAlerts(anyLong(), anyLong(), any());
    }

    @Test
    void getExpense_ShouldReturnFilteredExpense(){
        Pageable pageable = PageRequest.of(0,10);

        Page<Expense> expensePage =    new PageImpl<>(List.of(expense));

        when(currentUserService.getCurrentUserID())
                .thenReturn(userId);

        when(expenseRepo.findExpensesWithFilters(userId,10L, Expense.PaymentMode.UPI,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                pageable))
                .thenReturn(expensePage);

        Page<ExpenseResponse> response =expenseService.getExpenses(
                        10L,Expense.PaymentMode.UPI,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31),
                        pageable
                );
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(100L, response.getContent().get(0).getId());
        assertEquals("Food", response.getContent().get(0).getCategoryName());

        verify(expenseRepo).findExpensesWithFilters(
                userId,10L,Expense.PaymentMode.UPI,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                pageable
        );
    }
}
