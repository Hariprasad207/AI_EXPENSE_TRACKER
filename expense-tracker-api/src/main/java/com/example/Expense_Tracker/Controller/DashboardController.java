package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Service.DashboardService;
import com.example.Expense_Tracker.dto.dashboard.DashboardSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse>  getDashboardSummary(){
        Long userId = currentUserService.getCurrentUserID();
        DashboardSummaryResponse response = dashboardService.getDashboardSummary(userId);
        return ResponseEntity.ok(response);
    }
}
