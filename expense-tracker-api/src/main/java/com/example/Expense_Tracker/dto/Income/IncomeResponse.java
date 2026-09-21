package com.example.Expense_Tracker.dto.Income;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncomeResponse {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate incomeDate;
    private String title;
    private String source;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
