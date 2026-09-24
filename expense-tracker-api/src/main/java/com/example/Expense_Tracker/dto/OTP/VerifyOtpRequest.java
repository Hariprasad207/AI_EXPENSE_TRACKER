package com.example.Expense_Tracker.dto.OTP;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VerifyOtpRequest {
    @NotBlank(message = "Enter the Mail id")
    @Email(message = "Pleaase Enter the active mail id")
    private String email;

    @NotBlank(message = "Enter the OTP")
    @Pattern(regexp = "\\d{6}",message = "OTP must be in 6 digits")
    private String otp;
}
