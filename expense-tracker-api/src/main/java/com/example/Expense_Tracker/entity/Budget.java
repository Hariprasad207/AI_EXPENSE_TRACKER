package com.example.Expense_Tracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "budgets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_cat_month_year",
                        columnNames = {
                                "user_id",
                                "category_id",
                                "month",
                                "year"
                        })}
)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id",nullable = false)
    private Long userId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "amount",nullable = false,precision = 12,scale = 2)
    private BigDecimal amount;

    @Column(name = "month",nullable = false)
    private Integer month;

    @Column(name = "year",nullable = false)
    private Integer year;
}
