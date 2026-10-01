package com.example.Expense_Tracker.dto.UserProfile.NotificationRemainder;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Setter
@Getter
public class NotificationSettingsRequest {

    @NotNull(message = "Expense reminder setting cannot be null")
    private Boolean expenseReminderEnabled;

    @NotNull(message = "Budget alert setting cannot be null")
    private Boolean budgetAlertEnabled;

    @NotNull(message = "AI insight setting cannot be null")
    private Boolean aiInsightEnabled;

    @NotNull(message = "System notification setting cannot be null")
    private Boolean systemNotificationEnabled;

    private LocalTime reminderTime;
}
