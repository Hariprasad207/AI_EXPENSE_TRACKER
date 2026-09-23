package com.example.Expense_Tracker.Exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AiChatExceptionHandler {

    @ExceptionHandler(AiChatException.class)
    public ResponseEntity<Map<String, Object>> handleAiChatException(AiChatException exception){
        Map<String,Object> response = new HashMap<>();

        response.put("success",false);
        response.put("message",exception.getMessage());
        response.put("timestamp", LocalDateTime.now());

        return  ResponseEntity.status(exception.getStatusCode()).body(response);
    }
}
