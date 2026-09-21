package com.example.Expense_Tracker.dto.LoginRegister;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotNull(message = "Required the email_d")
    @Email(message = "Please Provide the valid mail_id")
    private String email;

    @NotNull(message = "Enter the Password")
    private String password;
}
