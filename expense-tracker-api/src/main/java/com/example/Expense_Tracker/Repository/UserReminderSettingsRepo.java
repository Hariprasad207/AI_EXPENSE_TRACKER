package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.UserReminderSettings;
import com.example.Expense_Tracker.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserReminderSettingsRepo extends JpaRepository<UserReminderSettings,Long> {

    Optional<UserReminderSettings> findByUser(Users user);

    @Query("""
       SELECT s
       FROM UserReminderSettings s
       WHERE s.user.id = :userId
       """)
    Optional<UserReminderSettings> findByUserId(@Param("userId") Long userId);
}
