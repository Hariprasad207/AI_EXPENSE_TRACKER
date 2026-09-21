package com.example.Expense_Tracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "ai_chat_history")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIChatHistory {

    public enum AiRole{
        USER,
        ASSISTANT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id",nullable = false)
    private Long userId;

    @Column(name = "conversation_id",nullable = false)
    private String conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false)
    private AiRole role;

    @Column(name = "message",nullable = false,columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at",nullable = false)
    private LocalDate createdAt;
}
