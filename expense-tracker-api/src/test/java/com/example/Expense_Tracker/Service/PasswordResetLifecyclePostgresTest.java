package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.BadRequestException;
import com.example.Expense_Tracker.Repository.PasswordResetOtpRepo;
import com.example.Expense_Tracker.Repository.PasswordResetTokenRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpRequest;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpResponse;
import com.example.Expense_Tracker.dto.Password.ForgetPasswordRequest;
import com.example.Expense_Tracker.dto.Password.ResetPasswordRequest;
import com.example.Expense_Tracker.entity.PasswordResetOtp;
import com.example.Expense_Tracker.entity.PasswordResetToken;
import com.example.Expense_Tracker.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ContextConfiguration(initializers = PostgresTestDatabaseGuard.class)
@ActiveProfiles("postgres-test")
class PasswordResetLifecyclePostgresTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordResetOtpRepo passwordResetOtpRepo;

    @Autowired
    private PasswordResetTokenRepo passwordResetTokenRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private EmailService emailService;

    @Test
    void completePasswordResetLifecycle_shouldConsumeOtpAndResetToken() {
        String email = "reset-lifecycle-" + UUID.randomUUID()
                + "@example.com";
        String oldPassword = "OldPassword123!";
        String newPassword = "NewPassword456!";

        Users user = transactionTemplate.execute(status ->
                userRepo.save(Users.builder()
                        .full_name("Password Reset Lifecycle Test")
                        .email(email)
                        .password_hash(passwordEncoder.encode(oldPassword))
                        .currency("INR")
                        .isActive(true)
                        .build())
        );

        assertNotNull(user);

        // Stage 1: Request an OTP.
        ForgetPasswordRequest forgotRequest = new ForgetPasswordRequest();
        forgotRequest.setEmail(email);

        authService.forgetPassword(forgotRequest);

        ArgumentCaptor<String> otpCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailService).sendTestMail(eq(email), otpCaptor.capture());

        String rawOtp = otpCaptor.getValue();

        assertNotNull(rawOtp);
        assertTrue(rawOtp.matches("\\d{6}"),
                "The generated OTP must contain six digits");

        // Stage 2: Verify the OTP and obtain the reset token.
        VerifyOtpRequest verifyRequest = new VerifyOtpRequest();
        verifyRequest.setEmail(email);
        verifyRequest.setOtp(rawOtp);

        VerifyOtpResponse verifyResponse = authService.verifyOtp(verifyRequest);

        assertNotNull(verifyResponse);
        assertEquals("OTP VERIFIED SUCCESSFULLY", verifyResponse.getMessage());

        String rawResetToken = verifyResponse.getResetToken();

        assertNotNull(rawResetToken);
        assertFalse(rawResetToken.isBlank());

        Boolean otpVerified = transactionTemplate.execute(status ->
                passwordResetOtpRepo.findTopByUserOrderByCreatedAtDesc(user)
                        .map(PasswordResetOtp::getVerified)
                        .orElse(false)
        );

        assertEquals(Boolean.TRUE, otpVerified, "The OTP must be marked verified in the database");

        // Stage 3: Reset the password with the issued token.
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setEmail(email);
        resetRequest.setResetToken(rawResetToken);
        resetRequest.setNewPassword(newPassword);
        resetRequest.setConfirmPassword(newPassword);

        assertDoesNotThrow(() -> authService.resetPassword(resetRequest));

        // Stage 4: Check the persisted password and token state.
        Boolean newPasswordMatches = transactionTemplate.execute(status -> {
            Users persistedUser = userRepo.findByEmail(email).orElseThrow();

            assertTrue(passwordEncoder.matches(
                    newPassword, persistedUser.getPassword_hash()));
            assertFalse(passwordEncoder.matches(
                    oldPassword, persistedUser.getPassword_hash()));

            PasswordResetToken persistedToken =
                    passwordResetTokenRepo.findAll().stream()
                            .filter(token -> token.getUser().getId()
                                    .equals(user.getId()))
                            .filter(token -> passwordEncoder.matches(
                                    rawResetToken, token.getTokenHash()))
                            .findFirst()
                            .orElseThrow(() ->
                                    new AssertionError(
                                            "The issued reset token must be persisted"));

            assertNotEquals(rawResetToken, persistedToken.getTokenHash(),
                    "The raw reset token must not be stored");

            assertTrue(persistedToken.getUsed(),
                    "The reset token must be marked as used");

            return passwordEncoder.matches(
                    newPassword, persistedUser.getPassword_hash());
        });

        assertEquals(Boolean.TRUE, newPasswordMatches);

        // Stage 5: The same reset token must not work a second time.
        assertThrows(BadRequestException.class,
                () -> authService.resetPassword(resetRequest));
    }
}
