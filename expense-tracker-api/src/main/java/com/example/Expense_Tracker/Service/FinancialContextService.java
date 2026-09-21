package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.AiInsightRepo;
import com.example.Expense_Tracker.Repository.BudgetRepo;
import com.example.Expense_Tracker.Repository.ExpenseRepo;
import com.example.Expense_Tracker.Repository.IncomeRepo;
import com.example.Expense_Tracker.dto.AiChat.FinancialContext;
import com.example.Expense_Tracker.entity.AiInsight;
import com.example.Expense_Tracker.entity.Budget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialContextService {

    private final ExpenseRepo expenseRepo;
    private final IncomeRepo incomeRepo;
    private final BudgetRepo budgetRepo;
    private final AiInsightRepo aiInsightRepo;

    public FinancialContext buildFinancialContext(Long userId){
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1);

        Integer month = today.getMonthValue();
        Integer year = today.getYear();

        BigDecimal totalIncome = safeAmount(incomeRepo.getTotalIncomeForDateRange(userId,startDate,endDate));
        BigDecimal totalExpense = safeAmount(expenseRepo.getTotalExpenseForDateRange(userId,startDate,endDate));
        BigDecimal totalSavings = totalIncome.subtract(totalExpense);

        List<CategorySpendingData> categorySpending =   expenseRepo.getCategoryExpenseSummary(userId,startDate,endDate)
                .stream()
                .map(summary -> new CategorySpendingData(
                        summary.getCategoryId(),summary.getCategoryName(),safeAmount(summary.getTotalAmount())))
                .toList();

        List<FinancialContext.CategorySpending> categorySpendingList = categorySpending.stream()
                .map(item -> FinancialContext.CategorySpending.builder()
                        .categoryId(item.categoryId())
                        .categoryName(item.categoryName())
                        .totalAmount(item.totalAmount())
                        .build())
                .toList();

        List<Budget>    budgets = budgetRepo.findByUserIdAndMonthAndYear(userId,month,year);

        List<FinancialContext.BudgetInformation> budgetInformationList = budgets.stream()
                .map(budget -> FinancialContext.BudgetInformation.builder()
                        .id(budget.getId())
                        .categoryId(budget.getCategoryId())
                        .amount(budget.getAmount())
                        .month(budget.getMonth())
                        .year(budget.getYear())
                        .build())
                .toList();

        List<AiInsight> aiInsights = aiInsightRepo.findByUserIdOrderByCreatedAtDesc(userId);

        List<FinancialContext.ExistingInsight> existingInsightList = aiInsights.stream()
                .map(insight -> FinancialContext.ExistingInsight.builder()
                        .id(insight.getId())
                        .title(insight.getTitle())
                        .message(insight.getMessage())
                        .type(insight.getType().name())
                        .month(insight.getMonth())
                        .year(insight.getYear())
                        .build()
                ).toList();

        return FinancialContext.builder()
                .month(month)
                .year(year)
                .totalExpense(totalExpense)
                .savings(totalSavings)
                .totalIncome(totalIncome)
                .categorySpendings(categorySpendingList)
                .budgetInformations(budgetInformationList)
                .existingInsights(existingInsightList)
                .build();
    }

    private BigDecimal safeAmount(BigDecimal amount){
        if(amount == null)  return BigDecimal.ZERO;
        return amount;
    }

    private record CategorySpendingData(
            Long categoryId,
            String categoryName,
            BigDecimal totalAmount
    ) {
    }
}
