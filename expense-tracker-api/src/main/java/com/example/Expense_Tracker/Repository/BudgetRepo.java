package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepo extends JpaRepository<Budget,Long> {
    Optional<Budget> findByIdAndUserId(Long id,Long userId);
    List<Budget> findByUserId(Long userId);
    List<Budget> findByUserIdAndMonthAndYear(Long userId,Integer month,Integer year);

    Optional<Budget> findByUserIdAndCategoryIdIsNullAndMonthAndYear(
            Long userId,
            Integer month,
            Integer year
    );

    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(
      Long userId,
      Long categoryId,
      Integer month,
      Integer year
    );
}
