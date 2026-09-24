package com.example.Expense_Tracker.dto.OTP;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class VerifyOtpResponse {

    private String message;
    private String resetToken;
}
