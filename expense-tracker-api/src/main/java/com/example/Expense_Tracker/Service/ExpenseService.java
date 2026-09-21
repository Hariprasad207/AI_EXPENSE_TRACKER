package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Exception.ResourceNotFoundException;
import com.example.Expense_Tracker.Repository.CategoryRepo;
import com.example.Expense_Tracker.Repository.ExpenseRepo;
import com.example.Expense_Tracker.dto.Expense.CreateExpenseRequest;
import com.example.Expense_Tracker.dto.Expense.ExpenseResponse;
import com.example.Expense_Tracker.dto.Expense.UpdateExpenseRequest;
import com.example.Expense_Tracker.entity.Category;
import com.example.Expense_Tracker.entity.Expense;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepo expenseRepo;
    private final CategoryRepo categoryRepo;
    private final CurrentUserService currentUserService;
    private final BudgetAlertService budgetAlertService;

    public ExpenseResponse currentExpense(CreateExpenseRequest request){
        Long userId = currentUserService.getCurrentUserID();
        Category category = categoryRepo.findAvailableCategory(request.getCategoryId(), userId
                ).orElseThrow(() ->
                        new RuntimeException("Category not found")
                );

        Expense expense = Expense.builder()
                .userId(userId)
                .category(category)
                .amount(request.getAmount())
                .paymentMode(request.getPaymentMode())
                .expenseDate(request.getExpenseDate())
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        Expense savedExpense =  expenseRepo.save(expense);
        budgetAlertService.checkBudgetAlerts(
                userId,
                savedExpense.getCategory().getId(),
                savedExpense.getExpenseDate()
        );
        return mapToResponse(savedExpense);
    }

    private ExpenseResponse mapToResponse(Expense expense){
        return ExpenseResponse.builder()
                .id(expense.getId())
                .categoryId(expense.getCategory().getId())
                .categoryName(expense.getCategory().getName())
                .amount(expense.getAmount())
                .paymentMode(expense.getPaymentMode())
                .expenseDate(expense.getExpenseDate())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }

    public ExpenseResponse getExpenseById(Long id){
        Long userId = currentUserService.getCurrentUserID();

        Expense expense = expenseRepo.findByIdAndUserId(id,userId)
                .orElseThrow(()-> new RuntimeException("Expense is not found"));

        return mapToResponse(expense);
    }

    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request){

        Long userId = currentUserService.getCurrentUserID();
        Expense expense = expenseRepo.findByIdAndUserId(id,userId)
                .orElseThrow(()-> new RuntimeException("Expense is not found"));
        Category category = categoryRepo.findAvailableCategory(request.getCategoryId(),userId)
                .orElseThrow(()-> new ResourceNotFoundException("Category is not found"));

        Long oldCategoryId = expense.getCategory().getId();
        LocalDate oldExpenseDate = expense.getExpenseDate();

        expense.setCategory(category);
        expense.setAmount(request.getAmount());
        expense.setPaymentMode(request.getPaymentMode());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());

        Expense updatedExpense = expenseRepo.save(expense);

        budgetAlertService.checkBudgetAlerts(
                userId,
                oldCategoryId,
                oldExpenseDate
        );

        budgetAlertService.checkBudgetAlerts(
                userId,
                updatedExpense.getCategory().getId(),
                updatedExpense.getExpenseDate()
        );

        return mapToResponse(updatedExpense);
    }

    public void deleteExpense(Long id){
        Long userID =  currentUserService.getCurrentUserID();
        Expense expense = expenseRepo.findByIdAndUserId(id,userID)
                .orElseThrow(()->new RuntimeException("Expense not found"));

        Long categoryId = expense.getCategory().getId();
        LocalDate expenseDate = expense.getExpenseDate();
        expenseRepo.delete(expense);

        budgetAlertService.checkBudgetAlerts(
                userID,categoryId,expenseDate
        );
    }

    public Page<ExpenseResponse> getExpenses(
            Long categoryId,
            Expense.PaymentMode paymentMode,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {

        Long userId =
                currentUserService.getCurrentUserID();

        return expenseRepo
                .findExpensesWithFilters(
                        userId,
                        categoryId,
                        paymentMode,
                        fromDate,
                        toDate,
                        pageable
                )
                .map(this::mapToResponse);
    }
}
