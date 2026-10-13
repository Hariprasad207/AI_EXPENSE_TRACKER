package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.*;
import com.example.Expense_Tracker.dto.Notification.CreateNotificationRequest;
import com.example.Expense_Tracker.entity.Budget;
import com.example.Expense_Tracker.entity.Category;
import com.example.Expense_Tracker.entity.Notification;
import com.example.Expense_Tracker.entity.UserReminderSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {
    @Mock
    private ExpenseRepo expenseRepo;
    @Mock
    private BudgetRepo budgetRepo;
    @Mock
    private CategoryRepo categoryRepo;
    @Mock
    private NotificationRepo notificationRepo;
    @Mock
    private UserReminderSettingsRepo userReminderSettingsRepo;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private BudgetAlertService budgetAlertService;

    private Long userId;
    private Long categoryId;
    private LocalDate expenseDate;

    @BeforeEach void setup(){
        userId = 1L;
        categoryId = 10L;
        expenseDate = LocalDate.of(2026,10,6);
    }

    @Test void checkBudgetAlerts_shouldDoNothingWhenAlertIsDisabled(){
        UserReminderSettings settings = new UserReminderSettings();
        settings.setBudgetAlertEnabled(false);

        when(userReminderSettingsRepo.findByUserId(userId))
                .thenReturn(Optional.of(settings));

        budgetAlertService.checkBudgetAlerts(userId,categoryId,expenseDate);

        verify(budgetRepo,never())
                .findByUserIdAndCategoryIdAndMonthAndYear(
                        anyLong(),anyLong(),anyInt(),anyInt());

        verify(budgetRepo,never())
                .findByUserIdAndCategoryIdIsNullAndMonthAndYear(anyLong(),anyInt(),anyInt());
        verify(notificationService,never())
                .createNotification(anyLong(),any(CreateNotificationRequest.class));
    }

    @Test    void checkBudgetAlerts_shouldContinueWhenNoReminderSettingsExist(){
        when(userReminderSettingsRepo.findByUserId(userId))
                .thenReturn(Optional.empty());
        when(budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(
                userId,categoryId,10,2026))
                .thenReturn(Optional.empty());
        when(budgetRepo.findByUserIdAndCategoryIdIsNullAndMonthAndYear(
                userId,10,2026))
                .thenReturn(Optional.empty());

        budgetAlertService.checkBudgetAlerts(userId,categoryId,expenseDate);
        verify(budgetRepo)
                .findByUserIdAndCategoryIdAndMonthAndYear(userId,categoryId,10,2026);
        verify(budgetRepo)
                .findByUserIdAndCategoryIdIsNullAndMonthAndYear(userId,10,2026);
        verify(notificationService,never())
                .createNotification(any(),any(CreateNotificationRequest.class));
    }

    @Test void categoryBudget_shouldCreate80PercentAlert() {
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(categoryId)
                .amount(new BigDecimal("1000.00"))
                .month(10)
                .year(2026)
                .build();
        Category category = Category.builder()
                .id(categoryId)
                .name("Food")
                .build();

        when(userReminderSettingsRepo.findByUserId(userId))
                .thenReturn(Optional.empty());
        when(budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(userId,categoryId,10,2026))
                .thenReturn(Optional.of(budget));
        when(expenseRepo.getTotalExpenseForCategoryAndDateRange( eq(userId),
                eq(categoryId),
                eq(LocalDate.of(2026, 10, 1)),
                eq(LocalDate.of(2026, 11, 1))))
                .thenReturn(new BigDecimal("800.00"));
        when(categoryRepo.findById(categoryId))
                .thenReturn(Optional.of(category));
        when(notificationRepo.existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold(
                userId,categoryId,10,2026,80))
                .thenReturn(false);

        budgetAlertService.checkBudgetAlerts(userId,categoryId,expenseDate);
        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
        verify(notificationService)
                .createNotification(eq(userId),captor.capture());

        CreateNotificationRequest request = captor.getValue();
        assertEquals(categoryId, request.getCategoryId());
        assertEquals(10, request.getMonth());
        assertEquals(2026, request.getYear());
        assertEquals(80, request.getAlertThreshold());
        assertEquals("Budget Alert", request.getTitle());
        assertEquals(Notification.NotificationType.BUDGET_ALERT,request.getType());
    }

    @Test  void categoryBudget_shouldCreate90PercentAlert(){
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(categoryId)
                .amount(new BigDecimal("1000.00"))
                .month(10)
                .year(2026)
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .name("Food")
                .build();

        when(userReminderSettingsRepo.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(
                userId, categoryId, 10, 2026))
                .thenReturn(Optional.of(budget));

        when(expenseRepo.getTotalExpenseForCategoryAndDateRange(
                anyLong(),
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class)))
                .thenReturn(new BigDecimal("900.00"));

        when(categoryRepo.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(notificationRepo
                .existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold(
                        userId, categoryId, 10, 2026, 90))
                .thenReturn(false);

        budgetAlertService.checkBudgetAlerts(userId,categoryId,expenseDate);

        ArgumentCaptor<CreateNotificationRequest> captor =ArgumentCaptor.forClass(CreateNotificationRequest.class);

        verify(notificationService).createNotification(eq(userId), captor.capture());

        assertEquals(90, captor.getValue().getAlertThreshold());
        assertEquals("Budget Warning", captor.getValue().getTitle());
    }

    @Test void categoryBudget_shouldCreate100PercentAlert() {

        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(categoryId)
                .amount(new BigDecimal("1000.00"))
                .month(10)
                .year(2026)
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .name("Food")
                .build();

        when(userReminderSettingsRepo.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(
                userId, categoryId, 10, 2026))
                .thenReturn(Optional.of(budget));

        when(expenseRepo.getTotalExpenseForCategoryAndDateRange(
                anyLong(),
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class)))
                .thenReturn(new BigDecimal("1100.00"));

        when(categoryRepo.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(notificationRepo
                .existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold(
                        userId, categoryId, 10, 2026, 100))
                .thenReturn(false);

        budgetAlertService.checkBudgetAlerts(
                userId,
                categoryId,
                expenseDate
        );

        ArgumentCaptor<CreateNotificationRequest> captor =
                ArgumentCaptor.forClass(CreateNotificationRequest.class);

        verify(notificationService)
                .createNotification(eq(userId), captor.capture());

        assertEquals(100, captor.getValue().getAlertThreshold());
        assertEquals("Budget is Exceeded", captor.getValue().getTitle());
    }

    @Test void categoryBudget_shouldNotCreateAlertBelow80Percent() {
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(categoryId)
                .amount(new BigDecimal("1000.00"))
                .month(10)
                .year(2026)
                .build();

        when(userReminderSettingsRepo.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, 10, 2026))
                .thenReturn(Optional.of(budget));

        when(expenseRepo.getTotalExpenseForCategoryAndDateRange(
                anyLong(),
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class)))
                .thenReturn(new BigDecimal("700.00"));

        budgetAlertService.checkBudgetAlerts(userId,categoryId,expenseDate);

        verify(notificationService, never())
                .createNotification(anyLong(), any(CreateNotificationRequest.class));
    }

    @Test
    void categoryBudget_shouldNotCreateDuplicateAlert() {
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(categoryId)
                .amount(new BigDecimal("1000.00"))
                .month(10)
                .year(2026)
                .build();

        Category category = Category.builder()
                .id(categoryId)
                .name("Food")
                .build();

        when(userReminderSettingsRepo.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, 10, 2026))
                .thenReturn(Optional.of(budget));

        when(expenseRepo.getTotalExpenseForCategoryAndDateRange(
                anyLong(),
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class)))
                .thenReturn(new BigDecimal("800.00"));

        when(categoryRepo.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(notificationRepo
                .existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold(userId, categoryId, 10, 2026, 80))
                .thenReturn(true);

        budgetAlertService.checkBudgetAlerts(userId,categoryId,expenseDate);

        verify(notificationService, never())
                .createNotification(anyLong(), any(CreateNotificationRequest.class));
    }

    @Test
    void categoryBudget_shouldIgnoreZeroBudget() {
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(categoryId)
                .amount(BigDecimal.ZERO)
                .month(10)
                .year(2026)
                .build();

        when(userReminderSettingsRepo.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, 10, 2026))
                .thenReturn(Optional.of(budget));

        budgetAlertService.checkBudgetAlerts(userId,categoryId,expenseDate);

        verify(expenseRepo, never())
                .getTotalExpenseForCategoryAndDateRange(anyLong(),anyLong(),any(LocalDate.class),any(LocalDate.class));

        verify(notificationService, never())
                .createNotification(anyLong(), any(CreateNotificationRequest.class));
    }

    @Test
    void overallBudget_shouldCreateAlert() {
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(null)
                .amount(new BigDecimal("5000.00"))
                .month(10)
                .year(2026)
                .build();

        when(userReminderSettingsRepo.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, 10, 2026))
                .thenReturn(Optional.empty());

        when(budgetRepo.findByUserIdAndCategoryIdIsNullAndMonthAndYear(userId, 10, 2026))
                .thenReturn(Optional.of(budget));

        when(expenseRepo.getTotalExpenseForDateRange(eq(userId),
                eq(LocalDate.of(2026, 10, 1)),
                eq(LocalDate.of(2026, 11, 1))))
                .thenReturn(new BigDecimal("4500.00"));

        when(notificationRepo
                .existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold(userId, null, 10, 2026, 90))
                .thenReturn(false);

        budgetAlertService.checkBudgetAlerts(userId,categoryId,expenseDate);

        ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);

        verify(notificationService).createNotification(eq(userId), captor.capture());

        CreateNotificationRequest request = captor.getValue();

        assertNull(request.getCategoryId());
        assertEquals(90, request.getAlertThreshold());
        assertEquals("Overall Budget Warning",request.getTitle());
        assertEquals(Notification.NotificationType.BUDGET_ALERT,request.getType());
    }

    @Test
    void overallBudget_shouldNotCreateDuplicateAlert() {
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(null)
                .amount(new BigDecimal("5000.00"))
                .month(10)
                .year(2026)
                .build();

        when(userReminderSettingsRepo.findByUserId(userId))
                .thenReturn(Optional.empty());

        when(budgetRepo.findByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, 10, 2026))
                .thenReturn(Optional.empty());

        when(budgetRepo.findByUserIdAndCategoryIdIsNullAndMonthAndYear(userId, 10, 2026))
                .thenReturn(Optional.of(budget));

        when(expenseRepo.getTotalExpenseForDateRange(
                anyLong(),
                any(LocalDate.class),
                any(LocalDate.class)))
                .thenReturn(new BigDecimal("4500.00"));

        when(notificationRepo
                .existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold(userId, null, 10, 2026, 90))
                .thenReturn(true);

        budgetAlertService.checkBudgetAlerts(userId,categoryId,expenseDate);

        verify(notificationService, never())
                .createNotification(anyLong(), any(CreateNotificationRequest.class));
    }
}
