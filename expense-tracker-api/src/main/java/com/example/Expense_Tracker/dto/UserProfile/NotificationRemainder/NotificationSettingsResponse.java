package com.example.Expense_Tracker.dto.UserProfile.NotificationRemainder;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class NotificationSettingsResponse {

    private Boolean expenseReminderEnabled;
    private Boolean budgetAlertEnabled;
    private Boolean aiInsightEnabled;
    private Boolean systemNotificationEnabled;
    private LocalTime reminderTime;

}
