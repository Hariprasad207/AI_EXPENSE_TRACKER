package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.NotificationRepo;
import com.example.Expense_Tracker.dto.Notification.CreateNotificationRequest;
import com.example.Expense_Tracker.dto.Notification.NotificationResponse;
import com.example.Expense_Tracker.entity.Notification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepo notificationRepo;

    public NotificationResponse mapToResponse(Notification response){
        return NotificationResponse.builder()
                .id(response.getId())
                .title(response.getTitle())
                .message(response.getMessage())
                .type(response.getType())
                .isRead(response.isRead())
                .createdAt(response.getCreatedAt())
                .build();
    }

    public List<NotificationResponse> getNotification(Long userId){
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public long getUnreadCount(Long userId){
        return notificationRepo.countByUserIdAndReadFalse(userId);
    }

    public NotificationResponse markedAsRead(Long id,Long userId){
        Notification notification = notificationRepo.findByIdAndUserId(id,userId)
                .orElseThrow(()-> new RuntimeException("That notification is not found"));
        notification.setRead(true);
        Notification updatedResponse = notificationRepo.save(notification);
        return mapToResponse(updatedResponse);
    }

    public void markedAllAsRead(Long userId){
        List<Notification> notifications = notificationRepo.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        notifications.forEach(notification -> notification.setRead(true));
        notificationRepo.saveAll(notifications);
    }

    public void deleteNotification(Long userId,Long id){
        Notification notification = notificationRepo.findByIdAndUserId(id,userId)
                .orElseThrow(()->new RuntimeException("That Notification not found"));
        notificationRepo.delete(notification);
    }

    public NotificationResponse createNotification(Long usrId, CreateNotificationRequest request){
        Notification notification = Notification.builder()
                .userId(usrId)
                .categoryId(request.getCategoryId())
                .month(request.getMonth())
                .year(request.getYear())
                .alertThreshold(request.getAlertThreshold())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .read(false)
                .build();

        Notification savedNotification = notificationRepo.save(notification);
        return mapToResponse(savedNotification);
    }
}
