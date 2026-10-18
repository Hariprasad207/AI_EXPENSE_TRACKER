package com.example.Expense_Tracker.Exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid user ID or password");
    }
}