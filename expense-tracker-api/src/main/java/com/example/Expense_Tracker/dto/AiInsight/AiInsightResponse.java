package com.example.Expense_Tracker.dto.AiInsight;

import com.example.Expense_Tracker.entity.AiInsight;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInsightResponse {
    private Long id;
    private String  title;
    private String message;
    private AiInsight.InsightType type;
    private boolean isRead;
    private OffsetDateTime createdAt;
}
