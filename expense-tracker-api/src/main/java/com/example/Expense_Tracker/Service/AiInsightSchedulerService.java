package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiInsightSchedulerService {
    private final AiInsightService aiInsightService;
    private final UserRepo userRepo;

    @Scheduled(cron = "0 0 20 * * *")
    public void generateAiInsights(){
        log.info("Starting the Scheduled AI Insight generation");
        List<Users> users = userRepo.findByIsActiveTrue();

        for(Users user:users){
            Long userId = (long) user.getId();
            try{
                aiInsightService.analyzeMonthlyExpenseChange(userId);
                aiInsightService.analyzeHighestSpendingCategory(userId);
                aiInsightService.analyzeMonthlySavings(userId);
                aiInsightService.analyzeMonthlyBudget(userId);
                aiInsightService.analyzeUnusualSpending(userId);
                aiInsightService.analyzeUnusualCategorySpending(userId);

                log.info("Ai Insights Generated Successfully for user {}",userId);
            }
            catch (Exception e){
                log.error("Failed to generate AI insight for user {}",userId,e);
            }
        }
        log.info("Daily Ai Insight Generation completed Successfully");
    }

    @Scheduled(cron = "0 0 20 * * MON")
    public void generateWeeklyInsights() {

        log.info("Starting weekly AI insight generation");
        List<Users> users = userRepo.findByIsActiveTrue();

        for (Users user : users) {
            Long userId = (long) user.getId();
            try {
                aiInsightService.analyzeSpendingTrend(userId);
                log.info("Weekly AI insights generated successfully for user {}",userId);
            } catch (Exception e) {
                log.error("Failed to generate weekly AI insights for user {}",userId,e);
            }
        }

        log.info("Weekly AI insight generation completed");
    }
}
