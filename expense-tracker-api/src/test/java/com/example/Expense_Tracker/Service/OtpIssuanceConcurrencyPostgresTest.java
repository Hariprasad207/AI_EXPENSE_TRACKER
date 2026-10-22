package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.PasswordResetOtpRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.Password.ForgetPasswordRequest;
import com.example.Expense_Tracker.entity.PasswordResetOtp;
import com.example.Expense_Tracker.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(initializers = PostgresTestDatabaseGuard.class)
@ActiveProfiles("postgres-test")
class OtpIssuanceConcurrencyPostgresTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordResetOtpRepo passwordResetOtpRepo;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoBean
    private EmailService emailService;

    @Test
    void concurrentForgotPasswordRequests_shouldCreateOnlyOneOtp()
            throws Exception {

        String email = "otp-issuance-" + UUID.randomUUID()
                + "@example.com";

        Users user = transactionTemplate.execute(status ->
                userRepo.save(Users.builder()
                        .full_name("OTP Issuance Concurrency Test")
                        .email(email)
                        .password_hash("test-only-password-hash")
                        .currency("INR")
                        .isActive(true)
                        .build())
        );

        assertNotNull(user);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = executor.submit(() -> {
                ready.countDown();
                awaitStart(start);
                requestOtp(email);
            });

            Future<?> second = executor.submit(() -> {
                ready.countDown();
                awaitStart(start);
                requestOtp(email);
            });

            assertTrue(ready.await(10, TimeUnit.SECONDS),
                    "Both requests should be ready");

            start.countDown();

            first.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);

            List<PasswordResetOtp> issuedOtps =
                    transactionTemplate.execute(status ->
                            passwordResetOtpRepo
                                    .findTopByUserOrderByCreatedAtDesc(user)
                                    .map(List::of)
                                    .orElseGet(List::of)
                    );

            assertNotNull(issuedOtps);
            assertEquals(1, issuedOtps.size(),
                    "Concurrent requests should result in one OTP");

            verify(emailService, times(1))
                    .sendTestMail(eq(email), anyString());

        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private void requestOtp(String email) {
        ForgetPasswordRequest request = new ForgetPasswordRequest();
        request.setEmail(email);
        authService.forgetPassword(request);
    }

    private void awaitStart(CountDownLatch start) {
        try {
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Start barrier timed out");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Request interrupted before starting", ex);
        }
    }
}
