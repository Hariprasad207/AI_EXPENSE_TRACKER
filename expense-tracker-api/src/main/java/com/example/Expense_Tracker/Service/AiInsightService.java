package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.*;
import com.example.Expense_Tracker.Specification.CategoryExpenseSummary;
import com.example.Expense_Tracker.dto.AiInsight.AiInsightRequest;
import com.example.Expense_Tracker.dto.AiInsight.AiInsightResponse;
import com.example.Expense_Tracker.dto.Notification.CreateNotificationRequest;
import com.example.Expense_Tracker.entity.AiInsight;
import com.example.Expense_Tracker.entity.Budget;
import com.example.Expense_Tracker.entity.Notification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AiInsightService {

    private final ExpenseRepo expenseRepo;
    private final AiInsightRepo aiInsightRepo;
    private final IncomeRepo incomeRepo;
    private final BudgetRepo budgetRepo;
    private final CategoryRepo categoryRepo;

    private final NotificationService notificationService;
    private final UserReminderSettingsRepo userReminderSettingsRepo;


    // =========================================================
    // MAP AI INSIGHT TO RESPONSE
    // =========================================================
    private AiInsightResponse mapToResponse(AiInsight insight) {

        return AiInsightResponse.builder()
                .id(insight.getId())
                .title(insight.getTitle())
                .message(insight.getMessage())
                .type(insight.getType())
                .isRead(insight.isRead())
                .createdAt(insight.getCreatedAt())
                .build();
    }

    // =========================================================
    // GET ALL AI INSIGHTS
    // =========================================================

    public List<AiInsightResponse> getAllInsight(Long userId) {

        return aiInsightRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET UNREAD AI INSIGHTS
    // =========================================================

    public List<AiInsightResponse> getUnreadInsight(Long userId) {

        return aiInsightRepo.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET UNREAD COUNT
    // =========================================================

    public long getUnreadCount(Long userId) {

        return aiInsightRepo.countByUserIdAndReadFalse(userId);
    }

    // =========================================================
    // MARK AI INSIGHT AS READ
    // =========================================================

    public AiInsightResponse markAsRead(Long id, Long userId) {

        AiInsight insight = aiInsightRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() ->new RuntimeException("That insight is not found"));

        insight.setRead(true);
        AiInsight updatedInsight = aiInsightRepo.save(insight);
        return mapToResponse(updatedInsight);
    }

    // =========================================================
    // CREATE / UPDATE AI INSIGHT
    // =========================================================

    public AiInsightResponse createInsight(Long userId,AiInsightRequest request) {
        var existingInsight =aiInsightRepo.findByUserIdAndInsightKeyAndMonthAndYear(
                        userId,
                        request.getInsightKey(),
                        request.getMonth(),
                        request.getYear()
                );

        boolean isNewInsight = existingInsight.isEmpty();

        AiInsight insight = existingInsight.orElseGet(() ->AiInsight.builder()
                        .userId(userId)
                        .insightKey(request.getInsightKey())
                        .month(request.getMonth())
                        .year(request.getYear())
                        .read(false)
                        .build()
        );

        insight.setTitle(request.getTitle());
        insight.setMessage(request.getMessage());
        insight.setType(request.getType());

        AiInsight savedInsight = aiInsightRepo.save(insight);

        if (isNewInsight)
            createAiInsightNotification(userId, request);


        return mapToResponse(savedInsight);
    }

    // =========================================================
    // CREATE AI INSIGHT NOTIFICATION
    // =========================================================

    private void createAiInsightNotification(Long userId,AiInsightRequest request) {
        boolean aiInsightEnabled =userReminderSettingsRepo.findByUserId(userId)
                        .map(settings ->
                                Boolean.TRUE.equals(settings.getAiInsightEnabled()))
                        .orElse(true);

        if (!aiInsightEnabled)
            return;

        CreateNotificationRequest notificationRequest =CreateNotificationRequest.builder()
                        .title(request.getTitle())
                        .message(request.getMessage())
                        .type(Notification.NotificationType.AI_INSIGHT)
                        .month(request.getMonth())
                        .year(request.getYear())
                        .categoryId(null)
                        .alertThreshold(null)
                        .build();

        notificationService.createNotification(userId,notificationRequest);
    }


    // =========================================================
    // DELETE AI INSIGHT
    // =========================================================

    public void deleteInsight(Long id, Long userId) {
        AiInsight insight = aiInsightRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new RuntimeException("Ai insight is not found")
                );
        aiInsightRepo.delete(insight);
    }

    // =========================================================
    // MONTHLY EXPENSE CHANGE
    // =========================================================

    public void analyzeMonthlyExpenseChange(Long userId) {

        LocalDate today = LocalDate.now();
        LocalDate currentMonthStart = today.withDayOfMonth(1);
        LocalDate nextMonthStart =currentMonthStart.plusMonths(1);
        LocalDate previousMonthStart =currentMonthStart.minusMonths(1);
        LocalDate previousMonthEnd =currentMonthStart;

        BigDecimal currentMonthExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        currentMonthStart,
                        nextMonthStart
                );

        BigDecimal previousMonthExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        previousMonthStart,
                        previousMonthEnd
                );

        if (previousMonthExpense == null ||previousMonthExpense.compareTo(BigDecimal.ZERO) <= 0)
            return;

        BigDecimal difference =currentMonthExpense.subtract(previousMonthExpense);
        BigDecimal percentageChange =difference.multiply(BigDecimal.valueOf(100))
                        .divide(previousMonthExpense,2,RoundingMode.HALF_UP);

        String title;
        String message;
        AiInsight.InsightType type;

        String insightKey ="MONTHLY_EXPENSE_CHANGE";

        if (percentageChange.compareTo(BigDecimal.ZERO) > 0) {

            title = "spending Increased";
            message ="Your expense increased by "
                            + percentageChange
                            + "% compared to the last month";
            type = AiInsight.InsightType.HIGH_SPENDING;

        } else if (percentageChange.compareTo(BigDecimal.ZERO) < 0) {

            BigDecimal positivePercentage =percentageChange.abs();
            title = "Spending Decreseased";
            message ="Good job! Your expensed is reduced by "
                            + positivePercentage
                            + "% compared to last month";
            type = AiInsight.InsightType.SAVING;
        } else
            return;


        AiInsightRequest request =AiInsightRequest.builder()
                        .title(title)
                        .message(message)
                        .type(type)
                        .insightKey(insightKey)
                        .month(today.getMonthValue())
                        .year(today.getYear())
                        .build();

        createInsight(userId, request);
    }

    // =========================================================
    // HIGHEST SPENDING CATEGORY
    // =========================================================

    public void analyzeHighestSpendingCategory(Long userId) {

        LocalDate today = LocalDate.now();
        int month =today.getMonthValue();
        int year =today.getYear();

        String insightKey ="HIGHEST_SPENDING_CATEGORY";
        LocalDate startDate =today.withDayOfMonth(1);
        LocalDate endDate =startDate.plusMonths(1);

        List<CategoryExpenseSummary> categories =expenseRepo.getCategoryExpenseSummary(
                        userId,
                        startDate,
                        endDate
                );

        if (categories.isEmpty())
            return;

        CategoryExpenseSummary highestCategory =categories.get(0);
        String categoryName =highestCategory.getCategoryName();

        BigDecimal categoryAmount =highestCategory.getTotalAmount();
        String title ="Highest Spending Category";
        String message ="Your highest spending category of this month is "
                        + categoryName
                        + " with the total expense of "
                        + categoryAmount
                        + ".";

        AiInsightRequest request =AiInsightRequest.builder()
                        .insightKey(insightKey)
                        .title(title)
                        .message(message)
                        .type(AiInsight.InsightType.HIGH_SPENDING)
                        .month(month)
                        .year(year)
                        .build();


        createInsight(userId, request);
    }

    // =========================================================
    // MONTHLY SAVINGS
    // =========================================================

    public void analyzeMonthlySavings(Long userId) {

        LocalDate today = LocalDate.now();
        int month =today.getMonthValue();
        int year =today.getYear();
        String insightKey ="MONTHLY_SAVINGS_ANALYSIS";
        LocalDate startDate =today.withDayOfMonth(1);
        LocalDate endDate =today.plusMonths(1);

        BigDecimal totalIncome =incomeRepo.getTotalIncomeForDateRange(
                        userId,
                        startDate,
                        endDate
                );

        BigDecimal totalExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        startDate,
                        endDate
                );

        if (totalIncome == null ||totalIncome.compareTo(BigDecimal.ZERO) <= 0)
            return;


        BigDecimal savings =totalIncome.subtract(totalExpense);

        String title;
        String message;
        AiInsight.InsightType type;

        if (savings.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal savingsPercentage =savings
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalIncome,2,RoundingMode.HALF_UP);

            title ="Monthly Savings";
            message ="You saved "
                            + savings
                            + " this month, which is "
                            + savingsPercentage
                            + "% of your total income";
            type =AiInsight.InsightType.SAVING;
        } else if (savings.compareTo(BigDecimal.ZERO) == 0) {

            title ="No Savings in this month";
            message ="Your expenses are equal to the income this month.";
            type =AiInsight.InsightType.GENERAL;
        } else {

            BigDecimal deficit =savings.abs();
            title ="Spending Exceeds Income";
            message ="Your expense exceed your income by "
                            + deficit
                            + " this month";
            type =AiInsight.InsightType.HIGH_SPENDING;
        }


        AiInsightRequest request =AiInsightRequest.builder()
                        .insightKey(insightKey)
                        .title(title)
                        .message(message)
                        .type(type)
                        .month(month)
                        .year(year)
                        .build();

        createInsight(userId, request);
    }

// =========================================================
// MONTHLY BUDGET
// =========================================================

    public void analyzeMonthlyBudget(Long userId) {

        LocalDate today = LocalDate.now();

        int month = today.getMonthValue();
        int year = today.getYear();

        String insightKey = "BUDGET_PERFORMANCE";

        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1);

        List<Budget> budgets =budgetRepo.findByUserIdAndMonthAndYear(
                        userId,
                        month,
                        year
                );

        if (budgets.isEmpty())
            return;


        Budget mostCriticalBudget = null;

        BigDecimal highestPercentage = BigDecimal.ZERO;
        BigDecimal highestSpentAmount = BigDecimal.ZERO;

        for (Budget budget : budgets) {
            BigDecimal budgetAmount = budget.getAmount();
            if (budgetAmount == null ||budgetAmount.compareTo(BigDecimal.ZERO) <= 0)
                continue;

            BigDecimal totalSpend;

            if (budget.getCategoryId() != null) {
                totalSpend =expenseRepo.getTotalExpenseForCategoryAndDateRange(
                                userId,
                                budget.getCategoryId(),
                                startDate,
                                endDate
                        );
            } else {
                totalSpend =expenseRepo.getTotalExpenseForDateRange(
                                userId,
                                startDate,
                                endDate
                        );
            }

            if (totalSpend == null)
                totalSpend = BigDecimal.ZERO;

            BigDecimal percentageUsed =totalSpend.multiply(BigDecimal.valueOf(100))
                            .divide(budgetAmount,2,RoundingMode.HALF_UP);

            if (percentageUsed.compareTo(highestPercentage) > 0) {
                highestPercentage = percentageUsed;
                mostCriticalBudget = budget;
                highestSpentAmount = totalSpend;
            }
        }

        if (mostCriticalBudget == null)
            return;

        String budgetName;

        if (mostCriticalBudget.getCategoryId() == null)
            budgetName = "Overall";

         else
            budgetName =categoryRepo.findById(mostCriticalBudget.getCategoryId())
                            .map(category -> category.getName())
                            .orElse("Category");


        String title;
        String message;
        AiInsight.InsightType type;

        if (highestPercentage.compareTo(BigDecimal.valueOf(100)) >= 0) {

            BigDecimal exceedAmount =highestSpentAmount.subtract(mostCriticalBudget.getAmount());

            title = "Budget Exceed";
            message ="You have exceeded your "
                            + budgetName
                            + " budget by "
                            + exceedAmount
                            + ". Your current budget usage is "
                            + highestPercentage
                            + "%.";
            type = AiInsight.InsightType.BUDGET_WARNING;

        } else if (highestPercentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            title = "Budget Warning";
            message ="Your "
                            + budgetName
                            + " budget is close to its limit. "
                            + "You have used "
                            + highestPercentage
                            + "% of your budget.";
            type = AiInsight.InsightType.BUDGET_WARNING;

        } else {

            title = "Budget Performance";
            message ="Your budgets are currently under control. "
                            + "Your highest budget usage is "
                            + highestPercentage
                            + "% for "
                            + budgetName
                            + ".";
            type = AiInsight.InsightType.GENERAL;
        }

        AiInsightRequest request =AiInsightRequest.builder()
                        .insightKey(insightKey)
                        .title(title)
                        .message(message)
                        .type(type)
                        .month(month)
                        .year(year)
                        .build();

        createInsight(userId, request);
    }

    // =========================================================
    // WEEKLY SPENDING TREND
    // =========================================================

    public void analyzeSpendingTrend(Long userId) {

        LocalDate today =LocalDate.now();
        LocalDate currentMonthStart =today.withDayOfMonth(1);
        LocalDate threeMonthsAgoStart =currentMonthStart.minusMonths(3);
        LocalDate twoMonthsAgoStart =currentMonthStart.minusMonths(2);
        LocalDate previousMonthStart =currentMonthStart.minusMonths(1);

        BigDecimal threeMonthsAgoExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        threeMonthsAgoStart,
                        twoMonthsAgoStart
                );

        BigDecimal twoMonthsAgoExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        twoMonthsAgoStart,
                        previousMonthStart
                );

        BigDecimal previousMonthExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        previousMonthStart,
                        currentMonthStart
                );

        if (threeMonthsAgoExpense.compareTo(BigDecimal.ZERO) <= 0 &&
                twoMonthsAgoExpense.compareTo(BigDecimal.ZERO) <= 0 &&
                previousMonthExpense.compareTo(BigDecimal.ZERO) <= 0)

            return;

        boolean increasingTrend =threeMonthsAgoExpense.compareTo(twoMonthsAgoExpense) < 0 &&
                        twoMonthsAgoExpense.compareTo(previousMonthExpense) < 0;

        boolean decreaseTrend =threeMonthsAgoExpense.compareTo(twoMonthsAgoExpense) > 0 &&
                        twoMonthsAgoExpense.compareTo(previousMonthExpense) > 0;

        String title;
        String message;
        AiInsight.InsightType type;

        String insightKey ="SPENDING_TREND";

        if (increasingTrend) {

            title ="Spending Trend Increasing";
            message ="Your spending has increased constantly over the last three months";
            type =AiInsight.InsightType.SPENDING_TREND;
        } else if (decreaseTrend) {

            title ="Spending Trend Improving";
            message ="Your spending has decreased constantly over the last three months";
            type =AiInsight.InsightType.SAVING;
        } else
            return;

        AiInsightRequest request =AiInsightRequest.builder()
                        .title(title)
                        .insightKey(insightKey)
                        .message(message)
                        .type(type)
                        .month(today.getMonthValue())
                        .year(today.getYear())
                        .build();


        createInsight(userId, request);
    }

    // =========================================================
    // UNUSUAL SPENDING
    // =========================================================

    public void analyzeUnusualSpending(Long userId) {

        LocalDate today =LocalDate.now();
        LocalDate currentMonthStart =today.withDayOfMonth(1);
        LocalDate threeMonthsAgoStart =currentMonthStart.minusMonths(3);
        LocalDate twoMonthsAgoStart =currentMonthStart.minusMonths(2);
        LocalDate previousMonthStart =currentMonthStart.minusMonths(1);

        BigDecimal threeMonthsAgoExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        threeMonthsAgoStart,
                        twoMonthsAgoStart
                );

        BigDecimal twoMonthsAgoExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        twoMonthsAgoStart,
                        previousMonthStart
                );

        BigDecimal previousMonthExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        previousMonthStart,
                        currentMonthStart
                );

        if (threeMonthsAgoExpense.compareTo(BigDecimal.ZERO) <= 0 &&
                twoMonthsAgoExpense.compareTo(BigDecimal.ZERO) <= 0 &&
                previousMonthExpense.compareTo(BigDecimal.ZERO) <= 0)
            return;


        BigDecimal averageExpense =threeMonthsAgoExpense.add(twoMonthsAgoExpense)
                        .add(previousMonthExpense)
                        .divide(BigDecimal.valueOf(3),2,RoundingMode.HALF_UP);

        if (averageExpense.compareTo(BigDecimal.ZERO) <= 0)
            return;

        BigDecimal currentMonthExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        currentMonthStart,
                        today.plusDays(1)
                );

        if (currentMonthExpense == null ||currentMonthExpense.compareTo(BigDecimal.ZERO) <= 0)
            return;

        BigDecimal difference =currentMonthExpense.subtract(averageExpense);
        BigDecimal percentageIncrease =difference.multiply(BigDecimal.valueOf(100))
                        .divide(averageExpense,2,RoundingMode.HALF_UP);

        if (percentageIncrease.compareTo(BigDecimal.valueOf(30)) < 0)
            return;

        String title ="Unusual Spending Detected";
        String message ="Your current spending is "
                        + percentageIncrease
                        + "% higher than your average spending over the previous three months.";

        AiInsightRequest request =AiInsightRequest.builder()
                        .insightKey("UNUSUAL_SPENDING")
                        .title(title)
                        .message(message)
                        .type(AiInsight.InsightType.HIGH_SPENDING)
                        .month(today.getMonthValue())
                        .year(today.getYear())
                        .build();

        createInsight(userId, request);
    }

    // =========================================================
    // UNUSUAL CATEGORY SPENDING
    // =========================================================

    public void analyzeUnusualCategorySpending(Long userId) {

        LocalDate today =LocalDate.now();
        LocalDate currentMonthStart =today.withDayOfMonth(1);
        LocalDate threeMonthsAgoStart =currentMonthStart.minusMonths(3);
        LocalDate twoMonthsAgoStart =currentMonthStart.minusMonths(2);
        LocalDate previousMonthStart =currentMonthStart.minusMonths(1);

        List<CategoryExpenseSummary> currentCategories =expenseRepo.getCategoryExpenseSummary(
                        userId,
                        currentMonthStart,
                        today.plusDays(1));

        if (currentCategories.isEmpty())
            return;

        List<CategoryExpenseSummary> previousCategories =expenseRepo.getCategoryExpenseSummary(
                        userId,
                        threeMonthsAgoStart,
                        currentMonthStart);

        if (previousCategories.isEmpty())
            return;

        CategoryExpenseSummary unusualCategory =null;
        BigDecimal highestIncrease =BigDecimal.ZERO;

        for (CategoryExpenseSummary currentCategory :currentCategories) {
            BigDecimal currentAmount =currentCategory.getTotalAmount();
            BigDecimal previousAmount =BigDecimal.ZERO;

            for (CategoryExpenseSummary previousCategory :previousCategories) {
                if (currentCategory.getCategoryId().equals(previousCategory.getCategoryId())) {
                    previousAmount =previousCategory.getTotalAmount();
                    break;
                }
            }

            if (previousAmount.compareTo(BigDecimal.ZERO) <= 0)
                continue;

            BigDecimal increase =currentAmount.subtract(previousAmount)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(previousAmount,2,RoundingMode.HALF_UP);

            if (increase.compareTo(highestIncrease) > 0) {
                highestIncrease =increase;
                unusualCategory =currentCategory;
            }
        }

        if (unusualCategory == null ||highestIncrease.compareTo(BigDecimal.valueOf(30)) < 0)
            return;

        String categoryName =unusualCategory.getCategoryName();
        String title ="Unusual Category Spending";
        String message ="Your spending on "
                        + categoryName
                        + " has increased by "
                        + highestIncrease
                        + "% compared to the previous period.";

        AiInsightRequest request =AiInsightRequest.builder()
                        .insightKey("UNUSUAL_CATEGORY_SPENDING")
                        .title(title)
                        .message(message)
                        .type(AiInsight.InsightType.HIGH_SPENDING)
                        .month(today.getMonthValue())
                        .year(today.getYear())
                        .build();

        createInsight(userId, request);
    }
}