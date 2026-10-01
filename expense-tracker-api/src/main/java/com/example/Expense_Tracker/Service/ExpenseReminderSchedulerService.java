package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.ExpenseRepo;
import com.example.Expense_Tracker.Repository.NotificationRepo;
import com.example.Expense_Tracker.Repository.UserReminderSettingsRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.Notification.CreateNotificationRequest;
import com.example.Expense_Tracker.entity.Notification;
import com.example.Expense_Tracker.entity.UserReminderSettings;
import com.example.Expense_Tracker.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseReminderSchedulerService {

    private final UserRepo userRepo;
    private final ExpenseRepo expenseRepo;
    private final NotificationRepo notificationRepo;
    private final UserReminderSettingsRepo userReminderSettingsRepo;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void checkExpenseRemainders() {

        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalDate today = LocalDate.now();

        log.info("Checking expense remainders at {}", now);

        List<Users> users = userRepo.findByIsActiveTrue();

        for (Users user : users) {

            Long userId = (long) user.getId();

            try {

                UserReminderSettings settings =
                        userReminderSettingsRepo.findByUserId(userId)
                                .orElse(null);

                if (settings == null) {
                    log.info("No reminder settings found for user {}", userId);
                    continue;
                }

                log.info(
                        "User {} - Expense Reminder Enabled: {}, Reminder Time: {}",
                        userId,
                        settings.getExpenseReminderEnabled(),
                        settings.getReminderTime()
                );

                if (!Boolean.TRUE.equals(settings.getExpenseReminderEnabled())) {
                    continue;
                }

                LocalTime reminderTime = settings.getReminderTime();

                if (reminderTime == null) {
                    log.info("No reminder time configured for user {}", userId);
                    continue;
                }

                LocalTime configuredTime =
                        reminderTime.withSecond(0).withNano(0);

                log.info(
                        "User {} - Current Time: {}, Configured Reminder Time: {}",
                        userId,
                        now,
                        configuredTime
                );

                /*
                 * Do not create the reminder before the configured time.
                 * After the configured time, the duplicate check below
                 * ensures only one reminder is created per day.
                 */
                if (now.isBefore(configuredTime)) {
                    continue;
                }

                long expenseCount =
                        expenseRepo.countExpensesForUserOnDate(userId, today);

                log.info(
                        "User {} - Expenses recorded today: {}",
                        userId,
                        expenseCount
                );

                if (expenseCount > 0) {
                    log.info(
                            "User {} already recorded an expense today. No reminder needed.",
                            userId
                    );
                    continue;
                }

                OffsetDateTime startOfDay =
                        today.atStartOfDay(ZoneId.systemDefault())
                                .toOffsetDateTime();

                OffsetDateTime startOfTomorrow =
                        today.plusDays(1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toOffsetDateTime();

                boolean reminderAlreadyExists =
                        notificationRepo.existsByUserIdAndTypeAndCreatedAtBetween(
                                userId,
                                Notification.NotificationType.EXPENSE_REMINDER,
                                startOfDay,
                                startOfTomorrow
                        );

                if (reminderAlreadyExists) {
                    log.info(
                            "Expense reminder already exists for user {} today",
                            userId
                    );
                    continue;
                }

                createExpenseReminder(userId, today);

                log.info(
                        "Expense reminder created for user {}",
                        userId
                );

            } catch (Exception e) {

                log.error(
                        "Failed to process the expense reminder for user {}",
                        userId,
                        e
                );
            }
        }
    }
    
    private void createExpenseReminder(Long userId,LocalDate today){
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("Expense Reminder")
                .message("You haven't recorded any expense today."+
                        "Don't Forget to record your expense")
                .type(Notification.NotificationType.EXPENSE_REMINDER)
                .categoryId(null)
                .month(today.getMonthValue())
                .year(today.getYear())
                .alertThreshold(null)
                .build();

        notificationService.createNotification(userId,request);
    }
}
