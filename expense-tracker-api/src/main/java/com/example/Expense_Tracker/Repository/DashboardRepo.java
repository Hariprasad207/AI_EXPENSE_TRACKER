package com.example.Expense_Tracker.Repository;

import com.example.Expense_Tracker.dto.dashboard.CategoryExpenseResponse;
import com.example.Expense_Tracker.entity.Expense;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class DashboardRepo {
    private final JdbcTemplate jdbcTemplate;

    @Getter
    @AllArgsConstructor
    public static class CategoryExpenseProjection {
        private String categoryName;
        private BigDecimal amount;
    }

    @Getter
    @AllArgsConstructor
    public static class PaymentModeProjection {
        private Expense.PaymentMode paymentMode;
        private BigDecimal amount;
    }

    public DashboardRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BigDecimal getTotalIncome(Long userId){
        String sql = """
                SELECT COALESCE(SUM(amount),0)
                FROM income
                WHERE user_id = ?
                """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class,userId);
    }

    public BigDecimal getTotalExpense(Long userId){
        String sql = """
                SELECT COALESCE(SUM(amount),0)
                FROM expenses
                WHERE user_id=?
                """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class,userId);
    }

    public List<CategoryExpenseProjection> getExpenseByCategory(Long userId) {
        String sql = """
                SELECT
                    c.name AS category_name,
                    COALESCE(SUM(e.amount), 0) AS amount
                FROM expenses e
                JOIN categories c
                    ON e.category_id = c.id
                WHERE e.user_id = ?
                GROUP BY c.id, c.name
                ORDER BY amount DESC
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) ->new CategoryExpenseProjection(
                                rs.getString("category_name"),
                                rs.getBigDecimal("amount")
                        ),
                userId
        );
    }

    public List<PaymentModeProjection> getExpenseByPaymentMode(Long userId){
        String sql = """
                SELECT
                    payment_mode,
                    COALESCE(SUM(amount),0) AS amount
                From expenses
                WHERE user_id=?
                GROUP BY payment_mode
                ORDER BY amount desc
                """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new PaymentModeProjection(
                                Expense.PaymentMode.valueOf(
                                        rs.getString(
                                                "payment_mode"
                                        )
                                ),
                                rs.getBigDecimal("amount")
                        ),
                userId
        );
    }
}
