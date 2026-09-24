package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Users;
import com.example.Expense_Tracker.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOtpRepo extends JpaRepository<PasswordResetOtp,Long> {

    Optional<PasswordResetOtp> findTopByUserOrderByCreatedAtDesc(Users user);
    Optional<PasswordResetOtp> findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(Users user);
}
