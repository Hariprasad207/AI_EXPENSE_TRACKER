package com.example.Expense_Tracker.dto.AiChat;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialContext {
    private Integer month;
    private Integer year;

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal savings;

    private List<CategorySpending> categorySpendings;
    private List<BudgetInformation> budgetInformations;
    private List<ExistingInsight> existingInsights;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public  static class CategorySpending{
        private Long categoryId;
        private String categoryName;
        private BigDecimal totalAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static  class BudgetInformation{
        private Long id;
        private Long categoryId;
        private BigDecimal amount;
        private Integer month;
        private Integer year;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static  class ExistingInsight{
        private Long id;
        private String title;
        private String message;
        private String type;
        private Integer month;
        private Integer year;
    }
}
