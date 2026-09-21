package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<Notification> findByIdAndUserId(
            Long id,
            Long userId
    );


    long countByUserIdAndReadFalse(
            Long userId
    );


    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            Long userId
    );

    boolean existsByUserIdAndCategoryIdAndMonthAndYearAndAlertThreshold(
            Long userId,
            Long categoryId,
            Integer month,
            Integer year,
            Integer alertThreshold
    );
}