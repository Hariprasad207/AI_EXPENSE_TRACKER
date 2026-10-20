package com.example.Expense_Tracker.Service;

public interface AiProvider {
    String generateResponse(String systemPrompt,String userMessage);
}
