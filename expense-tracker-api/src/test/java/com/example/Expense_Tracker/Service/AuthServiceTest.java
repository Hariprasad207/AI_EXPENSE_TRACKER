package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Config.JWTService;
import com.example.Expense_Tracker.Exception.BadRequestException;
import com.example.Expense_Tracker.Repository.LoginHistroyRepo;
import com.example.Expense_Tracker.Repository.PasswordResetOtpRepo;
import com.example.Expense_Tracker.Repository.PasswordResetTokenRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.LoginRegister.LoginRequest;
import com.example.Expense_Tracker.dto.LoginRegister.LoginResponse;
import com.example.Expense_Tracker.dto.LoginRegister.RegisterRequest;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpRequest;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpResponse;
import com.example.Expense_Tracker.dto.Password.ForgetPasswordRequest;
import com.example.Expense_Tracker.dto.Password.ResetPasswordRequest;
import com.example.Expense_Tracker.entity.LoginHistory;
import com.example.Expense_Tracker.entity.PasswordResetOtp;
import com.example.Expense_Tracker.entity.PasswordResetToken;
import com.example.Expense_Tracker.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTService jwtService;

    @Mock
    private LoginHistroyRepo loginHistroyRepo;

    @Mock
    private PasswordResetOtpRepo passwordResetOtpRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordResetTokenRepo passwordResetTokenRepo;

    @InjectMocks
    private AuthService authService;

    private Users user;

    @BeforeEach
    void setUp(){
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
    void register_shouldCreateUser(){
        RegisterRequest request = new RegisterRequest();

        request.setFullName("Hari");
        request.setEmail("hari@example.com");
        request.setPassword("password@123");

        when(userRepo.existsByEmail("hari@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password@123"))
                .thenReturn("hashed-password");

        authService.register(request);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        verify(userRepo).save(userCaptor.capture());
        Users savedUser = userCaptor.getValue();

        assertEquals("Hari",savedUser.getFull_name());
        assertEquals("hari@example.com",savedUser.getEmail());
        assertEquals("hashed-password",savedUser.getPassword_hash());
        assertEquals("INR",savedUser.getCurrency());
        assertTrue(savedUser.getIsActive());

        verify(passwordEncoder)
                .encode("password@123");
    }

    @Test
    void register_shouldRejectDuplicateEmail(){
        RegisterRequest request = new RegisterRequest();

        request.setFullName("HARI");
        request.setEmail("hari@example.com");
        request.setPassword("password@123");

        when(userRepo.existsByEmail("hari@example.com"))
                .thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,()->authService.register(request)
        );

        assertEquals("You are already Registered",exception.getMessage());

        verify(userRepo,never()).save(any(Users.class));
        verify(passwordEncoder,never()).encode(toString());
    }

    @Test
    void login_shouldReturnJWTTokenForValidCredential(){
        LoginRequest request = new LoginRequest();
        request.setEmail("hari@example.com");
        request.setPassword("password@123");

        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password@123","hashed-password"))
                .thenReturn(true);

        when(jwtService.generateToken(1L,"hari@example.com"))
                .thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token",response.getToken());

        verify(loginHistroyRepo).save(any(LoginHistory.class));
        verify(jwtService).generateToken(1L,"hari@example.com");
    }

    @Test
    void login_shouldReturnUnknownUser(){
        LoginRequest request = new LoginRequest();

        request.setEmail("unknown@example.com");
        request.setPassword("password@123");
        when(userRepo.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(
                RuntimeException.class,()-> authService.login(request)
        );
        assertEquals("Invalid user ID or password",exception.getMessage());

        verify(passwordEncoder,never())
                .matches(anyString(),anyString());
        verify(jwtService,never())
                .generateToken(anyLong(),anyString());
        verify(loginHistroyRepo,never())
                .save(any(LoginHistory.class));
    }

    @Test
    void login_shouldRejectIncorrectPassword(){
        LoginRequest request = new LoginRequest();
        request.setEmail("hari@example.com");
        request.setPassword("1230456789");

        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1230456789","hashed-password"))
                .thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,()-> authService.login(request)
        );
        assertEquals("Invalid user ID or password",exception.getMessage());

        verify(jwtService,never())
                .generateToken(anyLong(),anyString());
        verify(loginHistroyRepo,never())
                .save(any(LoginHistory.class));
    }

    @Test
    void forgetPassword_shouldCreateOtpAndSendEmail(){
        ForgetPasswordRequest request = new ForgetPasswordRequest();
        request.setEmail("hari@example.com");

        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordResetOtpRepo.findTopByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("otp-hash");

        authService.forgetPassword(request);
        verify(passwordResetOtpRepo).save(any(PasswordResetOtp.class));
        verify(emailService).sendTestMail(eq("hari@example.com"),anyString());
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    void forgetPassword_shouldDoNothingIfUserNotFound(){
        ForgetPasswordRequest request = new ForgetPasswordRequest();

        request.setEmail("dummy@test.com");
        when(userRepo.findByEmail("dummy@test.com"))
                .thenReturn(Optional.empty());
        authService.forgetPassword(request);
        verify(passwordResetOtpRepo,never())
                .save(any(PasswordResetOtp.class));
        verify(emailService,never())
                .sendTestMail(anyString(),anyString());
    }

    @Test
    void verifyOtp_shouldCreateResetTokenForValidOTP(){
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

        when(passwordResetOtpRepo.findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otp));

        when(passwordEncoder.matches("123456","otp-hash"))
                .thenReturn(true);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("reset-token-hash");

        when(passwordResetTokenRepo.findLatestUnusedToken(user))
                .thenReturn(Optional.empty());

        VerifyOtpResponse response = authService.verifyOtp(request);

        assertNotNull(response);
        assertEquals("OTP VERIFIED SUCCESSFULLY",response.getMessage());
        assertNotNull(response.getResetToken());
        assertTrue(otp.getVerified());
        verify(passwordResetOtpRepo).save(otp);
        verify(passwordResetTokenRepo).save(any(PasswordResetToken.class));
    }

    @Test
    void verifyOtp_shouldRejectInvalidOtp(){
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("hari@example.com");
        request.setOtp("999999");

        PasswordResetOtp otp =PasswordResetOtp.builder()
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

        when(passwordResetOtpRepo.findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otp));

        when(passwordEncoder.matches("999999","otp-hash"))
                .thenReturn(false);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.verifyOtp(request)
        );

        assertEquals("Invalid OTP. 4 attempts remaining.",exception.getMessage());
        assertEquals(1, otp.getAttempts());
        verify(passwordResetOtpRepo)
                .save(otp);
        verify(passwordResetTokenRepo, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void verifyOtp_shouldRejectExpiryOTp(){
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("hari@example.com");
        request.setOtp("999999");

        PasswordResetOtp otp =PasswordResetOtp.builder()
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

        when(passwordResetOtpRepo.findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(otp));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.verifyOtp(request)
        );

        assertEquals("OTP is expired. Request a new OTP",exception.getMessage());
        verify(passwordEncoder,never())
                .matches(anyString(),anyString());
    }
    @Test
    void resetPassword_shouldChangePasswordSuccessfully() {

        ResetPasswordRequest request =
                new ResetPasswordRequest();

        request.setEmail("HARI@EXAMPLE.COM");
        request.setResetToken("reset-token");
        request.setNewPassword("NewPassword@123");
        request.setConfirmPassword("NewPassword@123");

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .id(1L)
                        .user(user)
                        .tokenHash("token-hash")
                        .expiresAt(OffsetDateTime.now().plusMinutes(5))
                        .used(false)
                        .createdAt(OffsetDateTime.now())
                        .build();

        when(userRepo.findByEmail("hari@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordResetTokenRepo.findLatestUnusedToken(user))
                .thenReturn(Optional.of(resetToken));

        when(passwordEncoder.matches(
                "reset-token",
                "token-hash"
        )).thenReturn(true);

        when(passwordEncoder.matches(
                "NewPassword@123",
                "hashed-password"
        )).thenReturn(false);

        when(passwordEncoder.encode("NewPassword@123"))
                .thenReturn("new-password-hash");

        authService.resetPassword(request);

        assertEquals("new-password-hash",user.getPassword_hash());
        assertTrue(resetToken.getUsed());
        verify(userRepo).save(user);
        verify(passwordResetTokenRepo).save(resetToken);
        verify(passwordEncoder).encode("NewPassword@123");
    }

    @Test
    void resetPassword_shouldRejectDifferentPasswords() {
        ResetPasswordRequest request =new ResetPasswordRequest();

        request.setEmail("hari@example.com");
        request.setResetToken("reset-token");
        request.setNewPassword("NewPassword@123");
        request.setConfirmPassword("DifferentPassword");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.resetPassword(request)
        );

        assertEquals("New password and confirm password do not match.",exception.getMessage());
        verify(userRepo, never()).findByEmail(anyString());
        verify(passwordResetTokenRepo, never())
                .save(any(PasswordResetToken.class));
    }
}
