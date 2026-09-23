package com.example.Expense_Tracker.Exception;

public class AiChatException extends RuntimeException {
    private final int statusCode;

    public AiChatException(String message,int statusCode){
        super(message);
        this.statusCode = statusCode;
    }

    public AiChatException(String message,int statusCode,Throwable cause){
        super(message,cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode(){
        return statusCode;
    }
}
