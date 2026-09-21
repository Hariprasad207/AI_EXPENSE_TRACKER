package com.example.Expense_Tracker.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private BigDecimal savings;
    private List<CategoryExpenseResponse> expenseByCategory;
    private List<PaymentModeResponse> expenseByPaymentMode;
}
