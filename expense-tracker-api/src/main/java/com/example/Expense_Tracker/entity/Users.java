package com.example.Expense_Tracker.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "Full_name",nullable = false,length = 100)
    private String full_name;

    @Column(nullable = false,unique = true,length = 100)
    private String email;

    @Column(name="password_hash",nullable = false,length = 250)
    private String password_hash;

    @Column(length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "isActive")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "createdAt")
    private OffsetDateTime created_at;

    @UpdateTimestamp
    @Column(name="updatedAt")
    private OffsetDateTime updated_at;
}
