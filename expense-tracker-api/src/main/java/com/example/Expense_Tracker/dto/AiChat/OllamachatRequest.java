package com.example.Expense_Tracker.dto.AiChat;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OllamachatRequest {
    private String model;
    private List<Message> messages;
    private boolean stream;
    private Boolean think;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message{
        private String role;
        private String content;
    }
}
