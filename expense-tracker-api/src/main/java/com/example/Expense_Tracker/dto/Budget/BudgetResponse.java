package com.example.Expense_Tracker.dto.Budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class BudgetResponse {
    private Long id;

    private Long userId;

    private Long categoryId;

    private String categoryName;

    private BigDecimal amount;

    private Integer month;

    private Integer year;
}
