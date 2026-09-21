package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Service.AiInsightService;
import com.example.Expense_Tracker.dto.AiInsight.AiInsightResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-insights")
@RequiredArgsConstructor
public class AiInsightController {

    private final AiInsightService aiInsightService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<AiInsightResponse>> getAllInsight(){
        Long userId = currentUserService.getCurrentUserID();
        List<AiInsightResponse> insights = aiInsightService.getAllInsight(userId);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<AiInsightResponse>> getUnreadInsight(){
        Long userId = currentUserService.getCurrentUserID();
        List<AiInsightResponse> insight = aiInsightService.getUnreadInsight(userId);
        return ResponseEntity.ok(insight);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(){
        Long userId = currentUserService.getCurrentUserID();
        Long count = aiInsightService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<AiInsightResponse> markasRead(@PathVariable Long id){
        Long userId = currentUserService.getCurrentUserID();
        AiInsightResponse insight = aiInsightService.markAsRead(id,userId);
        return ResponseEntity.ok(insight);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInsight(@PathVariable Long id){
        Long userId = currentUserService.getCurrentUserID();
        aiInsightService.deleteInsight(id,userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/analyze")
    public ResponseEntity<Void> analyzeInsights() {
        Long userId =currentUserService.getCurrentUserID();

        aiInsightService.analyzeMonthlyExpenseChange(userId);
        aiInsightService.analyzeHighestSpendingCategory(userId);
        aiInsightService.analyzeMonthlySavings(userId);
        aiInsightService.analyzeMonthlyBudget(userId);
        aiInsightService.analyzeSpendingTrend(userId);
        aiInsightService.analyzeUnusualSpending(userId);
        aiInsightService.analyzeUnusualCategorySpending(userId);

        return ResponseEntity.ok().build();
    }
}
