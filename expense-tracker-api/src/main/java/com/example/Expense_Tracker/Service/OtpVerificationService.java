package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.PasswordResetOtpRepo;
import com.example.Expense_Tracker.Repository.PasswordResetTokenRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpRequest;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpResponse;
import com.example.Expense_Tracker.entity.PasswordResetOtp;
import com.example.Expense_Tracker.entity.PasswordResetToken;
import com.example.Expense_Tracker.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpVerificationService {

    private final UserRepo userRepo;
    private final PasswordResetOtpRepo passwordResetOtpRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OtpVerificationResult verify(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        Users user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            return OtpVerificationResult.failure(
                    "Invalid email or OTP. Please check your details or request a new OTP."
            );
        }

        PasswordResetOtp otp = passwordResetOtpRepo
                .findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user)
                .orElse(null);

        if (otp == null) {
            return OtpVerificationResult.failure(
                    "No active OTP found.Please request a new OTP"
            );
        }

        if (Boolean.TRUE.equals(otp.getVerified())) {
            return OtpVerificationResult.failure(
                    "This Otp is already verified.Please request a new OTP"
            );
        }

        if (otp.getExpiresAt().isBefore(OffsetDateTime.now())) {
            return OtpVerificationResult.failure(
                    "OTP is expired. Request a new OTP"
            );
        }

        if (otp.getAttempts() >= 5) {
            return OtpVerificationResult.failure(
                    "Tried Too much time. Request a new OTP"
            );
        }

        if (Boolean.TRUE.equals(otp.getInvalidated())) {
            return OtpVerificationResult.failure(
                    "This OTP is no longer valid. Please request a new OTP."
            );
        }

        if (!passwordEncoder.matches(request.getOtp(), otp.getOtpHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            passwordResetOtpRepo.saveAndFlush(otp);

            int remaining = 5 - otp.getAttempts();

            return OtpVerificationResult.failure(
                    remaining <= 0
                            ? "Too many incorrect attempts. Please request a new OTP."
                            : "Invalid OTP. " + remaining + " attempts remaining."
            );
        }

        otp.setVerified(true);
        passwordResetOtpRepo.save(otp);

        passwordResetTokenRepo.findLatestUnusedToken(user)
                .ifPresent(previousToken -> {
                    previousToken.setUsed(true);
                    passwordResetTokenRepo.save(previousToken);
                });

        String rawToken = UUID.randomUUID().toString();

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(passwordEncoder.encode(rawToken))
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .used(false)
                .createdAt(OffsetDateTime.now())
                .build();

        passwordResetTokenRepo.save(token);

        return OtpVerificationResult.success(
                VerifyOtpResponse.builder()
                        .message("OTP VERIFIED SUCCESSFULLY")
                        .resetToken(rawToken)
                        .build()
        );
    }
}