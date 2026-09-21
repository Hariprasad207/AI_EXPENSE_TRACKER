package com.example.Expense_Tracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "income" )
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id",nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false,precision = 12,scale = 2)
    private BigDecimal amount;

    @Column(name = "income_date",nullable = false)
    private LocalDate incomeDate;

    @Column(name = "title",nullable = false,length = 150)
    private String title;

    @Column(length = 100)
    private String source;

    @Column(name = "description",columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_date",nullable = false,updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_date")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected  void onCreated(){
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected  void onUpdate(){
        updatedAt = OffsetDateTime.now();
    }

}
