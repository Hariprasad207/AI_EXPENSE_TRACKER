package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.BadRequestException;
import com.example.Expense_Tracker.Repository.PasswordResetOtpRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpRequest;
import com.example.Expense_Tracker.entity.PasswordResetOtp;
import com.example.Expense_Tracker.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class OtpTransactionIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordResetOtpRepo passwordResetOtpRepo;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    @Test
    void fifthIncorrectOtpAttempt_shouldPersistAttemptCount() {
        String email = "otp-transaction-test@example.com";

        Users user = transactionTemplate.execute(status ->
                userRepo.save(Users.builder()
                        .full_name("OTP Transaction Test")
                        .email(email)
                        .password_hash("existing-password-hash")
                        .currency("INR")
                        .isActive(true)
                        .build())
        );

        assertNotNull(user);

        PasswordResetOtp otp = transactionTemplate.execute(status ->
                passwordResetOtpRepo.save(PasswordResetOtp.builder()
                        .user(user)
                        .otpHash(passwordEncoder.encode("123456"))
                        .expiresAt(OffsetDateTime.now().plusMinutes(5))
                        .attempts(4)
                        .verified(false)
                        .invalidated(false)
                        .createdAt(OffsetDateTime.now())
                        .build())
        );

        assertNotNull(otp);

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);
        request.setOtp("999999");

        assertThrows(
                BadRequestException.class,
                () -> authService.verifyOtp(request)
        );

        Integer persistedAttempts = transactionTemplate.execute(status ->
                passwordResetOtpRepo.findById(otp.getId())
                        .orElseThrow()
                        .getAttempts()
        );

        assertEquals(
                5,
                persistedAttempts,
                "The fifth failed OTP attempt must persist despite the API error"
        );
    }
}
