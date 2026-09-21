package com.example.Expense_Tracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_insight")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInsight {

    public enum InsightType {
        HIGH_SPENDING,
        SAVING,
        BUDGET_WARNING,
        SPENDING_TREND,
        GENERAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id",nullable = false)
    private Long userId;

    @Column(nullable = false,length = 150)
    private String title;

    @Column(name = "insight_key",nullable = false,length = 100)
    private String insightKey;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsightType type;

    @Column(name = "is_read")
    private boolean read=false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "month")
    private Integer month;

    @Column(name = "year")
    private Integer year;

    @PrePersist
    protected void onCreate(){
        if(createdAt==null){
            createdAt = OffsetDateTime.now();
        }
    }
}
