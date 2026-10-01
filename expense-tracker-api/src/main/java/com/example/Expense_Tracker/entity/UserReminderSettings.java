package com.example.Expense_Tracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "user_remainder_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReminderSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false,unique = true)
    private Users user;

    @Column(name = "expense_reminder_enabled",nullable = false)
    @Builder.Default
    private Boolean expenseReminderEnabled = true;

    @Column(name = "budget_alert_enabled",nullable = false)
    @Builder.Default
    private Boolean budgetAlertEnabled = true;

    @Column(name = "ai_insight_enabled",nullable = false)
    @Builder.Default
    private Boolean aiInsightEnabled = true;

    @Column(name = "system_notification_enabled",nullable = false)
    @Builder.Default
    private Boolean SystemNotificationEnabled = true;

    @Column(name = "reminder_time")
    @Builder.Default
    private LocalTime reminderTime = LocalTime.of(20, 0);
}
