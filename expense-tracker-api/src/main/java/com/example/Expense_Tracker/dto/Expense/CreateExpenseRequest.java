package com.example.Expense_Tracker.dto.Expense;

import com.example.Expense_Tracker.entity.Expense;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter

public class CreateExpenseRequest {

    @NotNull(message = "The catagory is required")
    private Long categoryId;

    @NotNull(message = "The amount is required")
    @DecimalMin(value = "0.01",message = "The amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment mode is required")
    private Expense.PaymentMode paymentMode;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotBlank(message = "Title is required")
    @Size(max = 150,message = "The title len is lesser than 150 character")
    private String title;

    private String description;
}
