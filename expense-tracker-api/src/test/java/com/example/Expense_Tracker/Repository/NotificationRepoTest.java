package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class NotificationRepoTest {
    @Autowired
    private NotificationRepo notificationRepo;

    private Notification unreadNotification;
    private Notification readNotification;

    @BeforeEach void setup(){
        notificationRepo.deleteAll();

        unreadNotification = notificationRepo.save(
                Notification.builder()
                        .userId(1L)
                        .categoryId(10L)
                        .month(10)
                        .year(2026)
                        .alertThreshold(80)
                        .title("Budget Alert")
                        .message("You have used 80% of your budget.")
                        .type(Notification.NotificationType.BUDGET_ALERT)
                        .read(false)
                        .createdAt(OffsetDateTime.parse( "2026-10-10T10:00:00+05:30"))
                        .build()
        );

        readNotification = notificationRepo.save(
                Notification.builder()
                        .userId(1L)
                        .title("AI Insight")
                        .message("Your spending has decreased.")
                        .type(Notification.NotificationType.AI_INSIGHT)
                        .read(true)
                        .createdAt(OffsetDateTime.parse("2026-10-11T10:00:00+05:30"))
                        .build()
        );

        notificationRepo.save(
                Notification.builder()
                        .userId(2L)
                        .title("System Notification")
                        .message("A system update is available.")
                        .type(Notification.NotificationType.SYSTEM)
                        .read(false)
                        .createdAt(OffsetDateTime.parse("2026-10-12T10:00:00+05:30"))
                        .build()
        );
    }

    @Test void savedNotification_shouldGenerateIdAndCreatedAt(){
        Notification notification = notificationRepo.save(
                Notification.builder()
                        .userId(3L)
                        .title("Reminder")
                        .message("Remember to record your expenses.")
                        .type(Notification.NotificationType.REMINDER)
                        .build()
        );

        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getCreatedAt()).isNotNull();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldReturnNewestFirst() {
        List<Notification> notifications =
                notificationRepo.findByUserIdOrderByCreatedAtDesc(1L);

        assertThat(notifications).hasSize(2);
        assertThat(notifications.get(0).getTitle())
                .isEqualTo("AI Insight");
        assertThat(notifications.get(1).getTitle())
                .isEqualTo("Budget Alert");
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_shouldNotReturnOtherUsersNotifications() {
        List<Notification> notifications =
                notificationRepo.findByUserIdOrderByCreatedAtDesc(1L);

        assertThat(notifications)
                .allMatch(notification -> notification.getUserId().equals(1L));
    }

    @Test
    void findByIdAndUserId_shouldReturnNotificationForCorrectUser() {
        Optional<Notification> result =
                notificationRepo.findByIdAndUserId(
                        unreadNotification.getId(), 1L
                );

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Budget Alert");
    }

    @Test
    void findByIdAndUserId_shouldNotReturnAnotherUsersNotification() {
        Optional<Notification> result =
                notificationRepo.findByIdAndUserId(
                        unreadNotification.getId(), 2L
                );

        assertThat(result).isEmpty();
    }

    @Test
    void countByUserIdAndReadFalse_shouldCountOnlyUnreadNotifications() {
        long count = notificationRepo.countByUserIdAndReadFalse(1L);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void findByUserIdAndReadFalseOrderByCreatedAtDesc_shouldReturnUnreadOnly() {
        List<Notification> notifications =
                notificationRepo.findByUserIdAndReadFalseOrderByCreatedAtDesc(1L);

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getTitle())
                .isEqualTo("Budget Alert");
        assertThat(notifications)
                .allMatch(notification -> !notification.isRead());
    }

    @Test
    void existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold_shouldReturnTrueWhenMatchingRecordExists() {
        boolean exists =
                notificationRepo
                        .existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold(
                                1L, 10L, 10, 2026, 80
                        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold_shouldReturnFalseWhenRecordDoesNotExist() {
        boolean exists =
                notificationRepo
                        .existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold(
                                1L, 10L, 10, 2026, 90
                        );

        assertThat(exists).isFalse();
    }

    @Test
    void existsByUserIdAndTypeAndCreatedAtBetween_shouldReturnTrueWithinDateRange() {
        OffsetDateTime start =
                OffsetDateTime.parse("2026-10-09T00:00:00+05:30");
        OffsetDateTime end =
                OffsetDateTime.parse("2026-10-11T23:59:59+05:30");

        boolean exists =notificationRepo.existsByUserIdAndTypeAndCreatedAtBetween(1L, Notification.NotificationType.BUDGET_ALERT, start, end);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndTypeAndCreatedAtBetween_shouldReturnFalseWhenNoMatchExists() {
        OffsetDateTime start =
                OffsetDateTime.parse("2026-10-09T00:00:00+05:30");
        OffsetDateTime end =
                OffsetDateTime.parse("2026-10-11T23:59:59+05:30");

        boolean exists =
                notificationRepo.existsByUserIdAndTypeAndCreatedAtBetween(
                        2L, Notification.NotificationType.BUDGET_ALERT, start, end
                );

        assertThat(exists).isFalse();
    }
}
