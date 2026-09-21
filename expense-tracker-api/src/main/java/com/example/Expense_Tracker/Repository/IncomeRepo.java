package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface IncomeRepo extends JpaRepository<Income,Long> {
    Optional<Income> findByIdAndUserId(Long id, Long userId);
    Page<Income> findByUserId(Long userId,Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(i.amount),0)
        FROM Income i
        WHERE i.userId = :userId
            AND i.incomeDate >= :startDate
            AND i.incomeDate < :endDate
""")
    BigDecimal getTotalIncomeForDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
