package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.Specification.CategoryExpenseSummary;
import com.example.Expense_Tracker.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ExpenseRepo extends JpaRepository<Expense,Long>, JpaSpecificationExecutor<Expense> {
    Optional<Expense> findByIdAndUserId(Long Id,Long userId);

    @Query("""
        SELECT e
        FROM Expense e
        WHERE e.userId = :userId
          AND (:categoryId IS NULL OR e.category.id = :categoryId)
          AND (:paymentMode IS NULL OR e.paymentMode = :paymentMode)
          AND (:fromDate IS NULL OR e.expenseDate >= :fromDate)
          AND (:toDate IS NULL OR e.expenseDate <= :toDate)
        """)
    Page<Expense> findExpensesWithFilters(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("paymentMode") Expense.PaymentMode paymentMode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.userId = :userId
      AND e.category.id = :categoryId
      AND e.expenseDate >= :startDate
      AND e.expenseDate < :endDate
""")
    BigDecimal getTotalExpenseForCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.userId = :userId
      AND e.expenseDate >= :startDate
      AND e.expenseDate < :endDate
""")
    BigDecimal getTotalExpenseForDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
    SELECT
        e.category.id AS categoryId,
        e.category.name AS categoryName,
        SUM(e.amount) AS totalAmount
    FROM Expense e
    WHERE e.userId = :userId
      AND e.expenseDate >= :startDate
      AND e.expenseDate < :endDate
    GROUP BY
        e.category.id,
        e.category.name
    ORDER BY SUM(e.amount) DESC
""")
    List<CategoryExpenseSummary> getCategoryExpenseSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}