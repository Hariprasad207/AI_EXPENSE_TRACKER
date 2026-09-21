package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Service.ExpenseService;
import com.example.Expense_Tracker.dto.Expense.CreateExpenseRequest;
import com.example.Expense_Tracker.dto.Expense.ExpenseResponse;
import com.example.Expense_Tracker.dto.Expense.UpdateExpenseRequest;
import com.example.Expense_Tracker.entity.Expense;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getExpenses(
            @RequestParam(required = false)  Long categoryId,
            @RequestParam(required = false) Expense.PaymentMode paymentMode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 10, sort = "expenseDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        System.out.println(">>> ExpenseController.getExpenses() REACHED");
        return ResponseEntity.ok(
                expenseService.getExpenses(categoryId, paymentMode, fromDate, toDate, pageable)
        );
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody CreateExpenseRequest request){
        ExpenseResponse response = expenseService.currentExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id){
        return ResponseEntity.ok( expenseService.getExpenseById(id) );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ExpenseResponse> deleteExpense(@PathVariable Long id){
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
 }
