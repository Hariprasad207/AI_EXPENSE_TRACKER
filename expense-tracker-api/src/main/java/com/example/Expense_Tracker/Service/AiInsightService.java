package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.*;
import com.example.Expense_Tracker.Specification.CategoryExpenseSummary;
import com.example.Expense_Tracker.dto.AiInsight.AiInsightRequest;
import com.example.Expense_Tracker.dto.AiInsight.AiInsightResponse;
import com.example.Expense_Tracker.entity.AiInsight;
import com.example.Expense_Tracker.entity.Budget;
import com.example.Expense_Tracker.entity.Category;
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

    private AiInsightResponse mapToResponse(AiInsight insight){
        return AiInsightResponse.builder()
                .id(insight.getId())
                .title(insight.getTitle())
                .message(insight.getMessage())
                .type(insight.getType())
                .isRead(insight.isRead())
                .createdAt(insight.getCreatedAt())
                .build();
    }

    public List<AiInsightResponse> getAllInsight(Long userId){
        return aiInsightRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AiInsightResponse> getUnreadInsight(Long userId){
        return aiInsightRepo.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public long getUnreadCount(Long userId){
        return aiInsightRepo.countByUserIdAndReadFalse(userId);
    }

    public AiInsightResponse markAsRead(Long id,Long userId){
        AiInsight insight = aiInsightRepo.findByIdAndUserId(id, userId)
                .orElseThrow(()-> new RuntimeException("That insight is not found"));
        insight.setRead(true);
        AiInsight updatedInsight = aiInsightRepo.save(insight);

        return mapToResponse(updatedInsight);
    }

    public AiInsightResponse createInsight(Long userId, AiInsightRequest request){

        AiInsight insight = aiInsightRepo.findByUserIdAndInsightKeyAndMonthAndYear(
                userId, request.getInsightKey(), request.getMonth(), request.getYear())
                .orElseGet(()->
                        AiInsight.builder()
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
        return mapToResponse(savedInsight);
    }

    public void deleteInsight(Long id,Long userId){
        AiInsight insight = aiInsightRepo.findByIdAndUserId(id, userId)
                .orElseThrow(()-> new RuntimeException("Ai insight is not found"));
        aiInsightRepo.delete(insight);
    }

    public void analyzeMonthlyExpenseChange(Long userId){
        LocalDate today = LocalDate.now();

        LocalDate currentMonthStart = today.withDayOfMonth(1);
        LocalDate nextMonthStart = currentMonthStart.plusMonths(1);

        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDate previousMonthEnd = currentMonthStart;

        BigDecimal currentMonthExpense = expenseRepo.getTotalExpenseForDateRange(
                userId,currentMonthStart,nextMonthStart
        );

        BigDecimal previousMonthExpense = expenseRepo.getTotalExpenseForDateRange(
                userId,previousMonthStart,previousMonthEnd
        );

        if(previousMonthExpense == null || previousMonthExpense.compareTo(BigDecimal.ZERO)<=0)
            return;

        BigDecimal difference = currentMonthExpense.subtract(previousMonthExpense);
        BigDecimal percentageChange = difference.multiply(BigDecimal.valueOf(100)).divide(previousMonthExpense,2, RoundingMode.HALF_UP);

        String title;
        String message;
        AiInsight.InsightType type;
        String insightKey = "MONTHLY_EXPENSE_CHANGE";

        if(percentageChange.compareTo(BigDecimal.ZERO)>0){
            title = "spending Increased";
            message = "Your expense increased by "+percentageChange+"% compared to the last month";
            type = AiInsight.InsightType.HIGH_SPENDING;
        }
        else if(percentageChange.compareTo(BigDecimal.ZERO)<0){
            BigDecimal positivePercentage = percentageChange.abs();
            title = "Spending Decreseased";
            message = "Good job! Your expensed is reduced by "+positivePercentage+"% compared to last month";
            type = AiInsight.InsightType.SAVING;
        }
        else return;

        AiInsightRequest request = AiInsightRequest.builder()
                .title(title)
                .message(message)
                .type(type)
                .insightKey(insightKey)
                .month(today.getMonthValue())
                .year(today.getYear())
                .build();

        createInsight(userId,request);
    }

    public void analyzeHighestSpendingCategory(Long userId){
        LocalDate today = LocalDate.now();

        int month = today.getMonthValue();
        int year = today.getYear();
        String insightKey = "HIGHEST_SPENDING_CATEGORY";

        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1);

        List<CategoryExpenseSummary> categories = expenseRepo.getCategoryExpenseSummary(userId,startDate,endDate);
        if(categories.isEmpty())    return;

        CategoryExpenseSummary highestCategory = categories.get(0);
        String categoryName = highestCategory.getCategoryName();
        BigDecimal categoryAmount = highestCategory.getTotalAmount();

        String title = "Highest Spending Category";
        String message = "Your highest spending category of this month is "+categoryName+
                "with the total expense of "+categoryAmount+".";

        AiInsightRequest request = AiInsightRequest.builder()
                .insightKey(insightKey)
                .title(title)
                .message(message)
                .type(AiInsight.InsightType.HIGH_SPENDING)
                .month(month)
                .year(year)
                .build();

        createInsight(userId,request);
    }

    public void analyzeMonthlySavings(Long userId){
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        String insightKey =    "MONTHLY_SAVINGS_ANALYSIS";

        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1);

        BigDecimal totalIncome = incomeRepo.getTotalIncomeForDateRange(userId,startDate,endDate);
        BigDecimal totalExpense = expenseRepo.getTotalExpenseForDateRange(userId,startDate,endDate);

        if(totalIncome==null || totalIncome.compareTo(BigDecimal.ZERO)<=0)  return;

        BigDecimal savings = totalIncome.subtract(totalExpense);
        String title;
        String message;
        AiInsight.InsightType type;

        if(savings.compareTo(BigDecimal.ZERO)>0){
            BigDecimal savingsPercentage = savings.multiply(BigDecimal.valueOf(100))
                    .divide(totalIncome,2,RoundingMode.HALF_UP);

            title = "Monthly Savings";
            message = "You saved "+savings+" this month,which is "+savingsPercentage+"% of your total income";
            type = AiInsight.InsightType.SAVING;
        } else if (savings.compareTo(BigDecimal.ZERO)==0) {
            title = "No Savings in this month";
            message = "Your expenses are equal to the income this month.";
            type = AiInsight.InsightType.GENERAL;
        }
        else {
            BigDecimal deficit = savings.abs();
            title = "Spending Exceeds Income";
            message =  "Your expense exceed you income by "+deficit+" this month";
            type = AiInsight.InsightType.HIGH_SPENDING;
        }

        AiInsightRequest request = AiInsightRequest.builder()
                .insightKey(insightKey).title(title).message(message).type(type).month(month).year(year)
                .build();

        createInsight(userId,request);
    }

    public void analyzeMonthlyBudget(Long userId){
        LocalDate today = LocalDate.now();

        int month = today.getMonthValue();
        int year = today.getYear();
        String insight = "BUDGET_PERFORMANCE";

        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = startDate.plusMonths(1);

        List<Budget> budgets = budgetRepo.findByUserIdAndMonthAndYear(userId,month,year);
        if(budgets.isEmpty())   return;

        Budget mostCriticalBudget = null;
        BigDecimal highestPercentage = BigDecimal.ZERO;
        BigDecimal highestSpentAmount = BigDecimal.ZERO;

        for(Budget budget:budgets){
            BigDecimal budgetAmount = budget.getAmount();
            if(budgetAmount==null || budgetAmount.compareTo(BigDecimal.ZERO)<=0)    continue;

            BigDecimal totalSpend;
            if(budget.getCategoryId() != null){
                totalSpend = expenseRepo.getTotalExpenseForCategoryAndDateRange(
                        userId, budget.getCategoryId() ,startDate,endDate
                );
            }
            else{
                totalSpend = expenseRepo.getTotalExpenseForDateRange(userId,startDate,endDate);
            }

            BigDecimal percentageUsed = totalSpend.multiply(BigDecimal.valueOf(100))
                    .divide(budgetAmount,2,RoundingMode.HALF_UP);

            if(percentageUsed.compareTo(highestPercentage)>0){
                highestPercentage =percentageUsed;
                mostCriticalBudget = budget;
                highestSpentAmount = totalSpend;
            }
        }
        if(mostCriticalBudget == null) return;
        String budgetName;

        if(mostCriticalBudget.getCategoryId() == null)  budgetName = "Overall";
        else    budgetName = categoryRepo.findById(mostCriticalBudget.getCategoryId())
                .map(category -> category.getName())
                .orElse("Category");

        String title;
        String message;
        AiInsight.InsightType type;

        if(highestPercentage.compareTo(BigDecimal.valueOf(100))>=0){
            BigDecimal exceedAmount = highestSpentAmount.subtract(mostCriticalBudget.getAmount());

            title = "Budget Exceed";
            message = "You have exceed your "+budgetName+" budget by "+exceedAmount+" .Your current Budget Usage is "
                    +highestPercentage+"%";
            type = AiInsight.InsightType.BUDGET_WARNING;
        }
        else if (highestPercentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            title ="Budget Warning";
            message ="Your "+ budgetName+ " budget is close to its limit. "+ "You have used "
                            + highestPercentage+ "% of your budget.";

            type =AiInsight.InsightType.BUDGET_WARNING;
        }
        else {
            title ="Budget Performance";
            message ="Your budgets are currently under control. "+ "Your highest budget usage is "+ highestPercentage+
                    "% for "+ budgetName+ ".";

            type =AiInsight.InsightType.GENERAL;
        }

        AiInsightRequest request = AiInsightRequest.builder()
                .insightKey(insight)
                .title(title)
                .message(message)
                .type(type)
                .month(month)
                .year(year)
                .build();

        createInsight(userId,request);
    }

    public void analyzeSpendingTrend(Long userId) {

        LocalDate today = LocalDate.now();
        LocalDate currentMonthStart =today.withDayOfMonth(1);
        LocalDate threeMonthsAgoStart =currentMonthStart.minusMonths(3);
        LocalDate twoMonthsAgoStart =currentMonthStart.minusMonths(2);
        LocalDate previousMonthStart =currentMonthStart.minusMonths(1);
        LocalDate nextMonthStart =currentMonthStart.plusMonths(1);

        BigDecimal threeMonthsAgoExpense = expenseRepo.getTotalExpenseForDateRange(
                userId,threeMonthsAgoStart,twoMonthsAgoStart
        );
        BigDecimal twoMonthsAgoExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,twoMonthsAgoStart,previousMonthStart
                );
        BigDecimal previousMonthExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,previousMonthStart,currentMonthStart
                );

        if (threeMonthsAgoExpense.compareTo(BigDecimal.ZERO) <= 0 &&
                twoMonthsAgoExpense.compareTo(BigDecimal.ZERO) <= 0
                && previousMonthExpense.compareTo(BigDecimal.ZERO) <= 0
        )       return;

        boolean increasingTrend = threeMonthsAgoExpense.compareTo(twoMonthsAgoExpense)<0 &&
                twoMonthsAgoExpense.compareTo(previousMonthExpense)<0;
        boolean decreaseTrend = threeMonthsAgoExpense.compareTo(twoMonthsAgoExpense)>0 &&
                twoMonthsAgoExpense.compareTo(previousMonthExpense)>0;

        String title;
        String message;
        AiInsight.InsightType type;
        String insightKey = "SPENDING_TREND";

        if(increasingTrend){
            title = "Spending Trend Increasing";
            message = "Your spending has increased constantly over the last three month";
            type = AiInsight.InsightType.SPENDING_TREND;
        } else if (decreaseTrend) {
            title = "Spending Trend Improving";
            message = "Your spending has decreased constantly over the last three month";
            type = AiInsight.InsightType.SAVING;
        }else return;

        AiInsightRequest request  = AiInsightRequest.builder()
                .title(title)
                .insightKey(insightKey)
                .message(message)
                .type(type)
                .month(today.getMonthValue())
                .year(today.getYear())
                .build();

        createInsight(userId,request);
    }

    public void analyzeUnusualSpending(Long userId) {

        LocalDate today = LocalDate.now();
        LocalDate currentMonthStart = today.withDayOfMonth(1);
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

        BigDecimal averageSpending =threeMonthsAgoExpense.add(twoMonthsAgoExpense).add(previousMonthExpense)
                        .divide(BigDecimal.valueOf(3),2,RoundingMode.HALF_UP);

        if (averageSpending.compareTo(BigDecimal.ZERO) <= 0)        return;

        LocalDate nextMonthStart =currentMonthStart.plusMonths(1);

        BigDecimal currentMonthExpense =expenseRepo.getTotalExpenseForDateRange(
                        userId,
                        currentMonthStart,
                        nextMonthStart);

        BigDecimal threshold =averageSpending.multiply(BigDecimal.valueOf(1.30));

        if (currentMonthExpense.compareTo(threshold) < 0)       return;

        BigDecimal difference =currentMonthExpense.subtract(averageSpending);
        BigDecimal percentageIncreased =difference.multiply(BigDecimal.valueOf(100))
                        .divide(averageSpending,2,RoundingMode.HALF_UP);

        String insightKey = "UNUSUAL_SPENDING";
        String title = "Unusual Spending";
        String message =
                "Your spending this month is "
                        + percentageIncreased
                        + "% higher than your average spending. "
                        + "You have spent ₹"
                        + currentMonthExpense
                        + " compared to your average expense of ₹"
                        + averageSpending
                        + ".";

        AiInsightRequest request =
                AiInsightRequest.builder()
                        .insightKey(insightKey)
                        .title(title)
                        .message(message)
                        .type(AiInsight.InsightType.HIGH_SPENDING)
                        .month(today.getMonthValue())
                        .year(today.getYear())
                        .build();

        createInsight(userId, request);
    }

    public void analyzeUnusualCategorySpending(Long userId) {

        LocalDate today = LocalDate.now();
        LocalDate currentMonthStart =today.withDayOfMonth(1);
        LocalDate nextMonthStart =currentMonthStart.plusMonths(1);
        LocalDate threeMonthsAgoStart =currentMonthStart.minusMonths(3);
        LocalDate twoMonthsAgoStart =currentMonthStart.minusMonths(2);
        LocalDate previousMonthStart =currentMonthStart.minusMonths(1);

        List<Category> categories =categoryRepo.findAvailableCategoryByType(userId,Category.CategoryType.EXPENSE);

        if (categories.isEmpty()) return;

        Category mostUnusual = null;

        BigDecimal highestPercentageIncrease =BigDecimal.ZERO;
        BigDecimal mostUnusualCurrentSpending =BigDecimal.ZERO;
        BigDecimal mostUnusualAverageSpending =BigDecimal.ZERO;

        for (Category category : categories) {
            Long categoryId = category.getId();

            BigDecimal threeMonthsAgoExpense =expenseRepo.getTotalExpenseForCategoryAndDateRange(
                            userId,
                            categoryId,
                            threeMonthsAgoStart,
                            twoMonthsAgoStart
                    );

            BigDecimal twoMonthsAgoExpense =expenseRepo.getTotalExpenseForCategoryAndDateRange(
                            userId,
                            categoryId,
                            twoMonthsAgoStart,
                            previousMonthStart
                    );

            BigDecimal previousMonthExpense =expenseRepo.getTotalExpenseForCategoryAndDateRange(
                            userId,
                            categoryId,
                            previousMonthStart,
                            currentMonthStart
                    );

            BigDecimal averageCategorySpending =threeMonthsAgoExpense.add(twoMonthsAgoExpense).add(previousMonthExpense)
                            .divide(BigDecimal.valueOf(3),2,RoundingMode.HALF_UP);

            if (averageCategorySpending.compareTo(BigDecimal.ZERO) <= 0)    continue;


            BigDecimal currentCategoryExpense =expenseRepo.getTotalExpenseForCategoryAndDateRange(
                            userId,
                            categoryId,
                            currentMonthStart,
                            nextMonthStart
                    );

            BigDecimal difference =currentCategoryExpense.subtract(averageCategorySpending);

            if (difference.compareTo(BigDecimal.ZERO) <= 0)     continue;

            BigDecimal percentageIncreased =difference.multiply(BigDecimal.valueOf(100))
                            .divide(averageCategorySpending,2,RoundingMode.HALF_UP);


            if (percentageIncreased.compareTo(BigDecimal.valueOf(30)) < 0)      continue;

            if (percentageIncreased.compareTo(highestPercentageIncrease) > 0) {
                highestPercentageIncrease =percentageIncreased;
                mostUnusualCurrentSpending =currentCategoryExpense;
                mostUnusual =category;
                mostUnusualAverageSpending =averageCategorySpending;
            }
        }

        if (mostUnusual == null)    return;


        String insightKey ="UNUSUAL_CATEGORY_SPENDING";
        String title ="Unusual Category Spending";
        String message ="Your "+ mostUnusual.getName()+ " spending is "+ highestPercentageIncrease
                        + "% higher than your normal monthly average. "+ "You have spent ₹"
                        + mostUnusualCurrentSpending+ " this month compared to your average of ₹"
                        + mostUnusualAverageSpending+ ".";

        AiInsightRequest request =
                AiInsightRequest.builder()
                        .insightKey(insightKey)
                        .type(AiInsight.InsightType.HIGH_SPENDING)
                        .title(title)
                        .message(message)
                        .month(today.getMonthValue())
                        .year(today.getYear())
                        .build();

        createInsight(userId, request);
    }
}
