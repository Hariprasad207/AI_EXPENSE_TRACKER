package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistroyRepo extends JpaRepository<LoginHistory,Long> {
}
