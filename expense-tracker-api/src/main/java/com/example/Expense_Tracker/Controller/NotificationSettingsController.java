package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Service.NotificationSettingsService;
import com.example.Expense_Tracker.dto.UserProfile.NotificationRemainder.NotificationSettingsRequest;
import com.example.Expense_Tracker.dto.UserProfile.NotificationRemainder.NotificationSettingsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationSettingsController {

    private final NotificationSettingsService notificationSettingsService;

    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> getNotificationSettings() {

        return ResponseEntity.ok(notificationSettingsService.getNotificationSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> updateNotificationSettings(
            @Valid @RequestBody NotificationSettingsRequest request
    ) {

        return ResponseEntity.ok(notificationSettingsService.updateNotificationSettings(request));
    }
}