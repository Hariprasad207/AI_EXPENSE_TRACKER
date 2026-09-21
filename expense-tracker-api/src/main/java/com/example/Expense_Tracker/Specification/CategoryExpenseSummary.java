package com.example.Expense_Tracker.Specification;

import java.math.BigDecimal;

public interface CategoryExpenseSummary {
    Long getCategoryId();
    String getCategoryName();
    BigDecimal getTotalAmount();
}
