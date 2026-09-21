package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Service.NotificationService;
import com.example.Expense_Tracker.dto.Notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(){
        Long userId = currentUserService.getCurrentUserID();
        List<NotificationResponse> notifications = notificationService.getNotification(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unreaded-count")
    public ResponseEntity<Long> CountOfUnreadNotification(){
        Long userId = currentUserService.getCurrentUserID();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markedAsRead(@PathVariable Long id){
        Long userId = currentUserService.getCurrentUserID();
        NotificationResponse notification = notificationService.markedAsRead(id,userId);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markedAllRead(){
        Long userId = currentUserService.getCurrentUserID();
        notificationService.markedAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id){
        Long userId = currentUserService.getCurrentUserID();
        notificationService.deleteNotification(userId,id);
        return ResponseEntity.noContent().build();
    }
    
}
