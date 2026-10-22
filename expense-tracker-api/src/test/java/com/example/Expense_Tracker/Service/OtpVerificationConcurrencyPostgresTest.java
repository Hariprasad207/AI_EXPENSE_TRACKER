package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.BadRequestException;
import com.example.Expense_Tracker.Repository.PasswordResetOtpRepo;
import com.example.Expense_Tracker.Repository.PasswordResetTokenRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpRequest;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpResponse;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(initializers = PostgresTestDatabaseGuard.class)
@ActiveProfiles("postgres-test")
class OtpVerificationConcurrencyPostgresTest {

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
    void concurrentOtpVerification_onlyOneRequestSucceeds() throws Exception {
        String email = "otp-concurrency-" + UUID.randomUUID()
                + "@example.com";
        String rawOtp = "123456";

        Users user = transactionTemplate.execute(status ->
                userRepo.save(Users.builder()
                        .full_name("OTP Verification Concurrency Test")
                        .email(email)
                        .password_hash(passwordEncoder.encode(
                                "InitialPassword123!"))
                        .currency("INR")
                        .isActive(true)
                        .build())
        );

        assertNotNull(user);

        PasswordResetOtp otp = transactionTemplate.execute(status ->
                passwordResetOtpRepo.save(
                        PasswordResetOtp.builder()
                                .user(user)
                                .otpHash(passwordEncoder.encode(rawOtp))
                                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                                .attempts(0)
                                .verified(false)
                                .invalidated(false)
                                .createdAt(OffsetDateTime.now())
                                .build()
                )
        );

        assertNotNull(otp);

        List<Long> existingTokenIds = transactionTemplate.execute(status ->
                passwordResetTokenRepo.findAll().stream()
                        .filter(token -> token.getUser().getId()
                                .equals(user.getId()))
                        .map(PasswordResetToken::getId)
                        .toList()
        );

        assertNotNull(existingTokenIds);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> first = executor.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Start barrier timed out");
                }
                return attemptVerification(email, rawOtp);
            });

            Future<Boolean> second = executor.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                            "Start barrier timed out");
                }
                return attemptVerification(email, rawOtp);
            });

            assertTrue(ready.await(10, TimeUnit.SECONDS),
                    "Both verification requests should be ready");

            start.countDown();

            boolean firstSucceeded = first.get(30, TimeUnit.SECONDS);
            boolean secondSucceeded = second.get(30, TimeUnit.SECONDS);

            assertNotEquals(firstSucceeded, secondSucceeded,
                    "Exactly one concurrent OTP verification must succeed");

            PasswordResetOtp persistedOtp = transactionTemplate.execute(
                    status -> passwordResetOtpRepo.findById(otp.getId())
                            .orElseThrow());

            assertNotNull(persistedOtp);
            assertTrue(persistedOtp.getVerified(),
                    "The OTP must be marked as verified");

            List<PasswordResetToken> newTokens =
                    transactionTemplate.execute(status ->
                            passwordResetTokenRepo.findAll().stream()
                                    .filter(token -> token.getUser().getId()
                                            .equals(user.getId()))
                                    .filter(token -> !existingTokenIds.contains(
                                            token.getId()))
                                    .toList()
                    );

            assertNotNull(newTokens);
            assertEquals(1, newTokens.size(),
                    "Concurrent verification must create exactly one new reset token");

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private boolean attemptVerification(String email, String rawOtp) {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);
        request.setOtp(rawOtp);

        try {
            VerifyOtpResponse response = authService.verifyOtp(request);
            return response != null;
        } catch (BadRequestException ex) {
            return false;
        }
    }
}
