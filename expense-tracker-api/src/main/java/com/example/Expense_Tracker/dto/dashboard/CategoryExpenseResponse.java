package com.example.Expense_Tracker.dto.dashboard;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryExpenseResponse {
    private String categoryName;
    private BigDecimal amount;
}
