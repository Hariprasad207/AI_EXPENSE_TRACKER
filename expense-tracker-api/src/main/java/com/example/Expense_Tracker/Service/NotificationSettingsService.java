package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Exception.BadRequestException;
import com.example.Expense_Tracker.Repository.UserReminderSettingsRepo;
import com.example.Expense_Tracker.dto.UserProfile.NotificationRemainder.NotificationSettingsRequest;
import com.example.Expense_Tracker.dto.UserProfile.NotificationRemainder.NotificationSettingsResponse;
import com.example.Expense_Tracker.entity.UserReminderSettings;
import com.example.Expense_Tracker.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class NotificationSettingsService {

    private final UserReminderSettingsRepo userReminderSettingsRepo;
    private final CurrentUserService currentUserService;

    private NotificationSettingsResponse mapToResponse(UserReminderSettings settings){
        return NotificationSettingsResponse.builder()
                .expenseReminderEnabled(settings.getExpenseReminderEnabled())
                .budgetAlertEnabled(settings.getBudgetAlertEnabled())
                .aiInsightEnabled(settings.getAiInsightEnabled())
                .systemNotificationEnabled(settings.getSystemNotificationEnabled())
                .reminderTime(settings.getReminderTime())
                .build();
    }

    private UserReminderSettings createDefaultSettings(Users user){
        UserReminderSettings settings = UserReminderSettings.builder()
                .user(user)
                .expenseReminderEnabled(true)
                .SystemNotificationEnabled(true)
                .budgetAlertEnabled(true)
                .aiInsightEnabled(true)
                .reminderTime(LocalTime.of(20,0))
                .build();

        return userReminderSettingsRepo.save(settings);
    }

    @Transactional
    public NotificationSettingsResponse getNotificationSettings(){
        Users currentUser = currentUserService.getCurrentUser();

        UserReminderSettings settings = userReminderSettingsRepo.findByUser(currentUser)
                .orElseGet(()-> createDefaultSettings(currentUser));

        return mapToResponse(settings);
    }

    @Transactional
    public NotificationSettingsResponse updateNotificationSettings(NotificationSettingsRequest request){
        Users currentUser = currentUserService.getCurrentUser();

        UserReminderSettings settings =   userReminderSettingsRepo.findByUser(currentUser)
                .orElseGet(()-> createDefaultSettings(currentUser));

        settings.setExpenseReminderEnabled(request.getExpenseReminderEnabled());
        settings.setBudgetAlertEnabled(request.getBudgetAlertEnabled());
        settings.setAiInsightEnabled(request.getAiInsightEnabled());
        settings.setSystemNotificationEnabled(request.getSystemNotificationEnabled());

        if(request.getReminderTime() != null )
            settings.setReminderTime(request.getReminderTime());

        UserReminderSettings updatedSettings = userReminderSettingsRepo.save(settings);
        return mapToResponse(updatedSettings);
    }
}
