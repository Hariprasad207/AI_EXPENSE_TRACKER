package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Exception.BadRequestException;
import com.example.Expense_Tracker.Repository.PasswordResetTokenRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.Password.ResetPasswordRequest;
import com.example.Expense_Tracker.entity.PasswordResetToken;
import com.example.Expense_Tracker.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
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
    class PasswordResetConcurrencyPostgresTest {

        @Autowired
        private AuthService authService;

        @Autowired
        private UserRepo userRepo;

        @Autowired
        private PasswordResetTokenRepo passwordResetTokenRepo;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private TransactionTemplate transactionTemplate;

        @MockitoBean
        private EmailService emailService;

        @Test
        void concurrentPasswordResets_onlyOneRequestSucceeds() throws Exception {
            String email = "reset-concurrency-" + UUID.randomUUID()
                    + "@example.com";
            String rawToken = UUID.randomUUID().toString();
            String initialPassword = "InitialPassword123!";
            String newPasswordOne = "NewPasswordOne123!";
            String newPasswordTwo = "NewPasswordTwo123!";

            Users user = transactionTemplate.execute(status ->
                    userRepo.save(Users.builder()
                            .full_name("Password Reset Concurrency Test")
                            .email(email)
                            .password_hash(passwordEncoder.encode(initialPassword))
                            .currency("INR")
                            .isActive(true)
                            .build())
            );

            assertNotNull(user);

            PasswordResetToken resetToken = transactionTemplate.execute(status ->
                    passwordResetTokenRepo.save(
                            PasswordResetToken.builder()
                                    .user(user)
                                    .tokenHash(passwordEncoder.encode(rawToken))
                                    .expiresAt(OffsetDateTime.now().plusMinutes(10))
                                    .used(false)
                                    .createdAt(OffsetDateTime.now())
                                    .build()
                    )
            );

            assertNotNull(resetToken);

            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);

            try {
                Future<Boolean> first = executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Start barrier timed out");
                    }
                    return attemptReset(email, rawToken, newPasswordOne);
                });

                Future<Boolean> second = executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Start barrier timed out");
                    }
                    return attemptReset(email, rawToken, newPasswordTwo);
                });

                assertTrue(ready.await(10, TimeUnit.SECONDS),
                        "Both reset requests should be ready");

                start.countDown();

                boolean firstSucceeded = first.get(30, TimeUnit.SECONDS);
                boolean secondSucceeded = second.get(30, TimeUnit.SECONDS);

                assertNotEquals(firstSucceeded, secondSucceeded,
                        "Exactly one concurrent reset request must succeed");

                Users persistedUser = userRepo.findByEmail(email).orElseThrow();
                PasswordResetToken persistedToken =
                        passwordResetTokenRepo.findById(resetToken.getId())
                                .orElseThrow();

                assertTrue(persistedToken.getUsed(),
                        "The reset token must be marked as used");

                boolean passwordOneMatches = passwordEncoder.matches(
                        newPasswordOne, persistedUser.getPassword_hash());
                boolean passwordTwoMatches = passwordEncoder.matches(
                        newPasswordTwo, persistedUser.getPassword_hash());

                assertNotEquals(passwordOneMatches, passwordTwoMatches,
                        "Only the successful request's password should be stored");

            } finally {
                start.countDown();
                executor.shutdownNow();
            }
        }

        private boolean attemptReset(
                String email,
                String rawToken,
                String newPassword
        ) {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setEmail(email);
            request.setResetToken(rawToken);
            request.setNewPassword(newPassword);
            request.setConfirmPassword(newPassword);

            try {
                authService.resetPassword(request);
                return true;
            } catch (BadRequestException ex) {
                return false;
            }
        }
    }
