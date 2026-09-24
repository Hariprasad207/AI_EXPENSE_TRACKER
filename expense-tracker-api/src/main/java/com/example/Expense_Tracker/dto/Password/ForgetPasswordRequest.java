package com.example.Expense_Tracker.dto.Password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter 
public class ForgetPasswordRequest {

    @NotBlank(message = "Enter The mail id")
    @Email(message = "Please provide a valid emaild Address")
    private String email;
}
