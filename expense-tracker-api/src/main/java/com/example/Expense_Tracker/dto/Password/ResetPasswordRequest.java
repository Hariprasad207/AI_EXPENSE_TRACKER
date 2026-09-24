package com.example.Expense_Tracker.dto.Password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Reset Token cannot be empty")
    private String resetToken;

    @NotBlank(message = "New Password cannot be empty")
    @Size(min = 8,max = 100,message = "New password is greater than 8 character")
    private String newPassword;

    @NotBlank(message="Confirm the new Password")
    private String confirmPassword;
}
