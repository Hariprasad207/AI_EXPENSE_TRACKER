package com.example.Expense_Tracker.dto.Password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Enter the Current password")
    private String currentPassword;

    @NotBlank(message = "Enter the New Password")
    @Size(min = 8,max = 100,message = "The password must be between 8 and 100 characters")
    private String newPassword;

    @NotBlank(message = "Enter the New password again")
    private String confirmPassword;
}