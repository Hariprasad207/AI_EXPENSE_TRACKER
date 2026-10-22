package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.dto.OTP.VerifyOtpResponse;

public record OtpVerificationResult(
        VerifyOtpResponse response,
        String errorMessage
) {
    public static OtpVerificationResult success(VerifyOtpResponse response) {
        return new OtpVerificationResult(response, null);
    }

    public static OtpVerificationResult failure(String message) {
        return new OtpVerificationResult(null, message);
    }

    public boolean isSuccess() {
        return response != null;
    }
}