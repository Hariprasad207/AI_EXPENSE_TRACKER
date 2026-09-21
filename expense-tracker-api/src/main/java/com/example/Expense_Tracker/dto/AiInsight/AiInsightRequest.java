package com.example.Expense_Tracker.dto.AiInsight;

import com.example.Expense_Tracker.entity.AiInsight;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInsightRequest {
    @NotBlank(message = "Insight title is Required")
    private String title;

    @NotBlank(message = "Insight message is REquired")
    private String message;

    @NotNull(message = "Insight type is necessary")
    private AiInsight.InsightType type;

    @NotBlank(message = "Insight key is required")
    private String insightKey;

    private Integer month;

    private Integer year;
}
