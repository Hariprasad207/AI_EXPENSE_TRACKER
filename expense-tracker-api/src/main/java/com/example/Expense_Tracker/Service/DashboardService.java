package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.DashboardRepo;
import com.example.Expense_Tracker.dto.dashboard.CategoryExpenseResponse;
import com.example.Expense_Tracker.dto.dashboard.DashboardSummaryResponse;
import com.example.Expense_Tracker.dto.dashboard.PaymentModeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepo dashboardRepo;

    public DashboardSummaryResponse getDashboardSummary(Long userId){

        BigDecimal totalIncome = dashboardRepo.getTotalIncome(userId);

        BigDecimal totalExpense = dashboardRepo.getTotalExpense(userId);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        List<CategoryExpenseResponse> expenseBycategory = dashboardRepo
                .getExpenseByCategory(userId)
                .stream()
                .map(item -> CategoryExpenseResponse.builder()
                        .categoryName(item.getCategoryName())
                        .amount(item.getAmount())
                        .build()
                ).toList();

        List<PaymentModeResponse> expenseByPaymentMode = dashboardRepo
                .getExpenseByPaymentMode(userId)
                .stream()
                .map(item -> PaymentModeResponse.builder()
                        .paymentMode(item.getPaymentMode())
                        .amount(item.getAmount())
                        .build()
                ).toList();

        return DashboardSummaryResponse.builder()
                .totalExpense(totalExpense)
                .totalIncome(totalIncome)
                .expenseByPaymentMode(expenseByPaymentMode)
                .expenseByCategory(expenseBycategory)
                .balance(balance)
                .savings(balance)
                .build();
    }
}
