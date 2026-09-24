package com.example.Expense_Tracker.dto.UserProfile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileRequest {
    @NotBlank(message = "Full name cannot be empty")
    @Size(max=100,message = "Full name must contain below 100 character")
    private String fullName;

    @NotBlank(message = "Email id cannot be empty")
    @Email(message = "Enter a vaild email Id")
    @Size(max = 100,message = "Email id not greater than 100 character")
    private String email;

    @NotBlank(message = "Currency needed to conatin values")
    @Size(max = 100,message = "Currency must below 100 character")
    private String currency;
}
