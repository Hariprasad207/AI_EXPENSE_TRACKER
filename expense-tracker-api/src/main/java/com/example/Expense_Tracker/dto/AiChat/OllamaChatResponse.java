package com.example.Expense_Tracker.dto.AiChat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaChatResponse {
    private String model;
    private Message message;
    private boolean done;

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message{
        private String role;
        private String content;
    }
}
