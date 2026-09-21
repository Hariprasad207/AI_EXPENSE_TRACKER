package com.example.Expense_Tracker.dto.Income;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeRequest {
    @NotNull(message = "Category is Required")
    private Long categoryId;

    @NotNull(message = "Amount is Required")
    @DecimalMin(value = "0.01",message = "Needed to enter the amount")
    private BigDecimal amount;

    @NotNull(message = "Income Date")
    private LocalDate incomeDate;

    @NotBlank(message = "Title is required")
    @Size(max=150,message = "Title is not exceed above 150 character")
    private String title;

    @Size(max = 100,message = "Source cannot exceed more than 100 character")
    private String source;

    private String description;
}
