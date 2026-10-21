package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Users;
import com.example.Expense_Tracker.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface PasswordResetOtpRepo extends JpaRepository<PasswordResetOtp,Long> {

    Optional<PasswordResetOtp> findTopByUserOrderByCreatedAtDesc(Users user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetOtp> findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(Users user);
}
