package com.example.Expense_Tracker.Controller;


import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Service.BudgetService;
import com.example.Expense_Tracker.dto.Budget.BudgetProgressResponse;
import com.example.Expense_Tracker.dto.Budget.BudgetRequest;
import com.example.Expense_Tracker.dto.Budget.BudgetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request){
        Long userId = currentUserService.getCurrentUserID();
        BudgetResponse response = budgetService.createBudget(userId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudget(@RequestParam Integer month,
                                                             @RequestParam Integer year){
        Long userId = currentUserService.getCurrentUserID();
        List<BudgetResponse> budget = budgetService.getBudgets(userId,month,year);
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable Long id){
        Long userId = currentUserService.getCurrentUserID();
        BudgetResponse budget = budgetService.getBudgetById(userId,id);
        return ResponseEntity.ok(budget);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(@PathVariable Long id,
                                                        @Valid @RequestBody BudgetRequest request){
        Long userId = currentUserService.getCurrentUserID();
        BudgetResponse budget = budgetService.updateBudgetById(userId,id,request);
        return ResponseEntity.ok(budget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BudgetResponse> deleteBudget(@PathVariable Long id){
        Long userId = currentUserService.getCurrentUserID();
        budgetService.deleteBudget(id,userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<BudgetProgressResponse> getBudgetProgress(@PathVariable Long id){
        Long userId = currentUserService.getCurrentUserID();
        BudgetProgressResponse response = budgetService.getBudgetProgress(userId,id);

        return ResponseEntity.ok(response);
    }
}
