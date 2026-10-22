package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.PasswordResetToken;
import com.example.Expense_Tracker.entity.Users;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PasswordResetToken> findFirstByUserAndUsedFalseOrderByCreatedAtDesc(
            Users user
    );
}