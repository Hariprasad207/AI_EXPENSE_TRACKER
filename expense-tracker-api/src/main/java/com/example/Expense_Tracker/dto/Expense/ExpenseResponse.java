package com.example.Expense_Tracker.dto.Expense;

import com.example.Expense_Tracker.entity.Expense;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class ExpenseResponse {
    private Long id;
    private  Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private Expense.PaymentMode paymentMode;
    private LocalDate expenseDate;
    private String title;
    private String description;
    private OffsetDateTime createdAt;
    private  OffsetDateTime updatedAt;

}
