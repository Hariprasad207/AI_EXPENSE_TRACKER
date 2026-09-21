package com.example.Expense_Tracker.dto.Notification;

import com.example.Expense_Tracker.entity.Notification;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateNotificationRequest {
    private Long categoryId;
    private Integer month;
    private Integer year;
    private Integer alertThreshold;
    private String title;
    private String message;
    private Notification.NotificationType type;
}
