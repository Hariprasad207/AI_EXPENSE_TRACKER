package com.example.Expense_Tracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Category {

    public enum CategoryType {
        EXPENSE,
        INCOME
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false,length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @Column(length = 50)
    private String icon;

    @Column(name = "is_custom")
    private Boolean isCustom = false;

}
