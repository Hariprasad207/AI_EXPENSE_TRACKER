package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.AiInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiInsightRepo extends JpaRepository<AiInsight, Long> {

    List<AiInsight> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<AiInsight> findByIdAndUserId(
            Long id,
            Long userId
    );

    List<AiInsight> findByUserIdAndReadFalseOrderByCreatedAtDesc(
            Long userId
    );

    long countByUserIdAndReadFalse(
            Long userId
    );

    boolean existsByUserIdAndInsightKeyAndMonthAndYear(
            Long userId,
            String insightKey,
            Integer month,
            Integer year
    );

    Optional<AiInsight> findByUserIdAndInsightKeyAndMonthAndYear(
            Long userId,String insightKey,Integer month,Integer year
    );
}