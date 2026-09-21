package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Service.IncomeService;
import com.example.Expense_Tracker.dto.Income.IncomeRequest;
import com.example.Expense_Tracker.dto.Income.IncomeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/income")
public class IncomeController {
    private final IncomeService incomeService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<Page<IncomeResponse>> getIncome(
            @PageableDefault(size = 10,sort = "incomeDate",direction = Sort.Direction.DESC)
            Pageable pageable) {

        Long userId =currentUserService.getCurrentUserID();
        Page<IncomeResponse> response =incomeService.getIncome(
                        userId,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(@Valid @RequestBody IncomeRequest request){
        Long userId = currentUserService.getCurrentUserID();
        IncomeResponse  response = incomeService.createIncome(request,userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncomeBuId(@PathVariable Long id){
        Long userId = currentUserService.getCurrentUserID();
        IncomeResponse response = incomeService.getIncomeId(id,userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(@PathVariable Long id,
                                                       @Valid @RequestBody IncomeRequest request){
        Long userId = currentUserService.getCurrentUserID();
        IncomeResponse response = incomeService.updateIncome(request,id,userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id){
        Long userId = currentUserService.getCurrentUserID();
        incomeService.deleteIncome(id,userId);
        return ResponseEntity.noContent().build();
    }
}
