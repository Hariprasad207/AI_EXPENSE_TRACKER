package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Repository.PasswordResetOtpRepo;
import com.example.Expense_Tracker.Repository.PasswordResetTokenRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpRequest;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpResponse;
import com.example.Expense_Tracker.entity.PasswordResetOtp;
import com.example.Expense_Tracker.entity.PasswordResetToken;
import com.example.Expense_Tracker.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OtpVerificationServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetOtpRepo passwordResetOtpRepo;

    @Mock
    private PasswordResetTokenRepo passwordResetTokenRepo;

    @InjectMocks
    private OtpVerificationService otpVerificationService;

    private Users user;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L)
                .full_name("Hari")
                .email("hari@example.com")
                .password_hash("hashed-password")
                .currency("INR")
                .isActive(true)
                .build();
    }

    @Test
    void verifyOtp_shouldCreateResetTokenForValidOTP() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("hari@example.com");
        request.setOtp("123456");

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .id(1L)
                .user(user)
                .otpHash("otp-hash")
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(false)
                .invalidated(false)
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepo
                .findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otp));

        when(passwordEncoder.matches("123456", "otp-hash"))
                .thenReturn(true);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("reset-token-hash");

        when(passwordResetTokenRepo.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.empty());

        OtpVerificationResult result =
                otpVerificationService.verify(request);

        assertTrue(result.isSuccess());

        VerifyOtpResponse response = result.response();

        assertNotNull(response);
        assertEquals("OTP VERIFIED SUCCESSFULLY", response.getMessage());
        assertNotNull(response.getResetToken());
        assertTrue(otp.getVerified());

        verify(passwordResetOtpRepo).save(otp);
        verify(passwordResetTokenRepo).save(any(PasswordResetToken.class));
    }

    @Test
    void verifyOtp_shouldRejectInvalidOtp() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("hari@example.com");
        request.setOtp("999999");

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .user(user)
                .otpHash("otp-hash")
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(false)
                .invalidated(false)
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepo
                .findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otp));

        when(passwordEncoder.matches("999999", "otp-hash"))
                .thenReturn(false);

        OtpVerificationResult result =
                otpVerificationService.verify(request);

        assertFalse(result.isSuccess());
        assertEquals(
                "Invalid OTP. 4 attempts remaining.",
                result.errorMessage()
        );
        assertEquals(1, otp.getAttempts());

        verify(passwordResetOtpRepo).saveAndFlush(otp);
        verify(passwordResetTokenRepo, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void verifyOtp_shouldRejectExpiryOtp() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("hari@example.com");
        request.setOtp("999999");

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .user(user)
                .otpHash("otp-hash")
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .attempts(0)
                .verified(false)
                .invalidated(false)
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepo
                .findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otp));

        OtpVerificationResult result =
                otpVerificationService.verify(request);

        assertFalse(result.isSuccess());
        assertEquals(
                "OTP is expired. Request a new OTP",
                result.errorMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
        verify(passwordResetTokenRepo, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void verifyOtp_shouldReturnGenericErrorForUnknownEmail() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("unknown@example.com");
        request.setOtp("123456");

        when(userRepo.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        OtpVerificationResult result =
                otpVerificationService.verify(request);

        assertFalse(result.isSuccess());
        assertEquals(
                "Invalid email or OTP. Please check your details or request a new OTP.",
                result.errorMessage()
        );

        verify(passwordResetOtpRepo, never())
                .findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(
                        any(Users.class)
                );

        verify(passwordResetOtpRepo, never())
                .save(any(PasswordResetOtp.class));

        verify(passwordResetTokenRepo, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void verifyOtp_shouldRejectFifthIncorrectAttempt() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("hari@example.com");
        request.setOtp("999999");

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .user(user)
                .otpHash("otp-hash")
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .attempts(4)
                .verified(false)
                .invalidated(false)
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepo
                .findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otp));

        when(passwordEncoder.matches("999999", "otp-hash"))
                .thenReturn(false);

        OtpVerificationResult result =
                otpVerificationService.verify(request);

        assertFalse(result.isSuccess());
        assertEquals(
                "Too many incorrect attempts. Please request a new OTP.",
                result.errorMessage()
        );
        assertEquals(5, otp.getAttempts());

        verify(passwordResetOtpRepo).saveAndFlush(otp);
        verify(passwordResetTokenRepo, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void verifyOtp_shouldRejectOtpWhenAttemptLimitAlreadyReached() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("hari@example.com");
        request.setOtp("123456");

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .user(user)
                .otpHash("otp-hash")
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .attempts(5)
                .verified(false)
                .invalidated(false)
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepo
                .findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otp));

        OtpVerificationResult result =
                otpVerificationService.verify(request);

        assertFalse(result.isSuccess());
        assertEquals(
                "Tried Too much time. Request a new OTP",
                result.errorMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
        verify(passwordResetTokenRepo, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void verifyOtp_shouldRejectInvalidatedOtpAsInactive() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("hari@example.com");
        request.setOtp("123456");

        // The repository query excludes invalidated OTPs.
        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepo
                .findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.empty());

        OtpVerificationResult result =
                otpVerificationService.verify(request);

        assertFalse(result.isSuccess());
        assertEquals(
                "No active OTP found.Please request a new OTP",
                result.errorMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
        verify(passwordResetTokenRepo, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void verifyOtp_shouldRejectAlreadyVerifiedOtp() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("hari@example.com");
        request.setOtp("123456");

        PasswordResetOtp otp = PasswordResetOtp.builder()
                .user(user)
                .otpHash("otp-hash")
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .attempts(0)
                .verified(true)
                .invalidated(false)
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetOtpRepo
                .findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otp));

        OtpVerificationResult result =
                otpVerificationService.verify(request);

        assertFalse(result.isSuccess());
        assertEquals(
                "This Otp is already verified.Please request a new OTP",
                result.errorMessage()
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
        verify(passwordResetTokenRepo, never())
                .save(any(PasswordResetToken.class));
    }
}
