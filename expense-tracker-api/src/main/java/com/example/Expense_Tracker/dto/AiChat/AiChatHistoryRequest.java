package com.example.Expense_Tracker.dto.AiChat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatHistoryRequest {
    @NotBlank(message = "Message cannot be empty")
    private String message;

    private String conversationId;
}
