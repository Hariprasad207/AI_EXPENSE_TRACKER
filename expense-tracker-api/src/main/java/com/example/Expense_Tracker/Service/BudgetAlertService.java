package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.BudgetRepo;
import com.example.Expense_Tracker.Repository.CategoryRepo;
import com.example.Expense_Tracker.Repository.ExpenseRepo;
import com.example.Expense_Tracker.Repository.NotificationRepo;
import com.example.Expense_Tracker.dto.Notification.CreateNotificationRequest;
import com.example.Expense_Tracker.entity.Budget;
import com.example.Expense_Tracker.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetAlertService {

    private final ExpenseRepo expenseRepo;
    private final BudgetRepo budgetRepo;
    private final NotificationService notificationService;
    private final CategoryRepo categoryRepo;
    private final NotificationRepo notificationRepo;

    public void checkBudgetAlerts(Long userId, Long categoryId, LocalDate expenseDate){
        int month = expenseDate.getMonthValue();
        int year = expenseDate.getYear();
        checkCategoryBudget(userId,categoryId,month,year);
        checkOverallBudget(userId,month,year);
    }

    private void checkCategoryBudget(Long userId, Long categoryId, int month, int year) {
        Optional<Budget> optionalBudget = budgetRepo
                .findByUserIdAndCategoryIdAndMonthAndYear(userId,categoryId,month,year);
        if(optionalBudget.isEmpty()){
            return;
        }
        Budget budget = optionalBudget.get();
        LocalDate startDate = LocalDate.of(year,month,1);
        LocalDate endDate = startDate.plusMonths(1);

        BigDecimal totalSpend = expenseRepo
                .getTotalExpenseForCategoryAndDateRange(userId,categoryId,startDate,endDate);

        BigDecimal amount = budget.getAmount();

        if(amount.compareTo(BigDecimal.ZERO)<=0)    return;
        BigDecimal percentageUsed = totalSpend.multiply(BigDecimal.valueOf(100))
                .divide(amount,2, RoundingMode.HALF_UP);

        String categoryName = categoryRepo
                .findById(categoryId)
                .map(category -> category.getName())
                .orElse("Category");

        createBudgetAlert(
                userId,
                categoryId,
                categoryName,
                month,
                year,
                percentageUsed
        );
    }

    private void createBudgetAlert(
            Long userId,
            Long categoryId,
            String budgetName,
            int month,
            int year,
            BigDecimal percentageUsed
    ){
        String title ;
        String message;
        Integer alertThreshold;

        if(percentageUsed.compareTo(BigDecimal.valueOf(100))>=0){
            alertThreshold = 100;
            title="Budget is Exceeded";
            message = "You have Exceed your "+budgetName+" budget.current usage "+percentageUsed+"%";
        }else if (percentageUsed.compareTo(BigDecimal.valueOf(90)) >= 0) {
            alertThreshold = 90;
            title = "Budget Warning";
            message ="You have used "+ percentageUsed+ "% of your "+ budgetName+ " budget.";
        }else if (percentageUsed.compareTo(BigDecimal.valueOf(80))>=0) {
            alertThreshold = 80;
            title = "Budget Alert ";
            message= "You are going to reach your "+budgetName+" budget.current Usage "+percentageUsed+"%";
        }
        else  return;

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .categoryId(categoryId)
                .month(month)
                .year(year)
                .alertThreshold(alertThreshold)
                .title(title)
                .message(message)
                .type(Notification.NotificationType.BUDGET_ALERT)
                .build();

        notificationService.createNotification(userId,request);
    }

    private void checkOverallBudget(Long userId, int month, int year) {
        Optional<Budget> optionalBudget =
                budgetRepo.findByUserIdAndCategoryIdIsNullAndMonthAndYear(userId,month,year);
        if(optionalBudget.isEmpty()) return;

        Budget budget = optionalBudget.get();
        LocalDate startDate = LocalDate.of(year,month,1);
        LocalDate endDate = startDate.plusMonths(1);
        BigDecimal totalSpend = expenseRepo.getTotalExpenseForDateRange(userId,startDate,endDate);
        BigDecimal budgetAmount = budget.getAmount();

        if(budgetAmount.compareTo(BigDecimal.ZERO)<=0)  return;

        BigDecimal percentageUsed = totalSpend.multiply(BigDecimal.valueOf(100))
                .divide(budgetAmount,2,RoundingMode.HALF_UP);

        createOverallBudgetAlert(userId,month,year,percentageUsed);
    }

    private void createOverallBudgetAlert(
            Long userId,
            int month,
            int year,
            BigDecimal percentageUsed
    ) {

        String title;
        String message;
        Integer alertThreshold;

        if (percentageUsed.compareTo(BigDecimal.valueOf(100)) >= 0) {
            alertThreshold = 100;
            title = "Overall Budget Exceeded";
            message =
                    "You have exceeded your overall monthly budget. " +
                            "Current usage: " +
                            percentageUsed +
                            "%";
        } else if (percentageUsed.compareTo(BigDecimal.valueOf(90)) >= 0) {
            alertThreshold = 90;
            title = "Overall Budget Warning";
            message ="You have used " +percentageUsed +"% of your overall monthly budget.";
        } else if (percentageUsed.compareTo(BigDecimal.valueOf(80)) >= 0) {
            alertThreshold = 80;
            title = "Overall Budget Alert";
            message ="You have used " +percentageUsed +"% of your overall monthly budget.";
        } else {
            return;
        }

        boolean alreadyExists =notificationRepo
                        .existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold(
                                userId,
                                null,
                                month,
                                year,
                                alertThreshold
                        );


        if (alreadyExists) {
            return;
        }


        CreateNotificationRequest request =CreateNotificationRequest.builder()
                        .title(title)
                        .message(message)
                        .type(Notification.NotificationType.BUDGET_ALERT)
                        .categoryId(null)
                        .month(month)
                        .year(year)
                        .alertThreshold(alertThreshold)
                        .build();
        notificationService.createNotification(userId,request);
    }

}
