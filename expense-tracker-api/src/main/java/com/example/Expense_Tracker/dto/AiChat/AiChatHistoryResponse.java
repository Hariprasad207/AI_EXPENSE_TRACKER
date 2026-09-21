package com.example.Expense_Tracker.dto.AiChat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiChatHistoryResponse {
    private String message;
    private String conversationId;
}
