package com.example.Expense_Tracker.dto.Budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetRequest {
    private Long categoryId;

    @NotNull(message = "Amount is Required")
    @DecimalMin(value = "0.01",message = "The amount is must greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is Required")
    @Min(value = 2000 , message = "The year must be greater than 2000")
    private Integer year;
}
