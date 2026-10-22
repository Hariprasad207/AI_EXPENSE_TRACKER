package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Config.JWTService;
import com.example.Expense_Tracker.Exception.BadRequestException;
import com.example.Expense_Tracker.Exception.InvalidCredentialsException;
import com.example.Expense_Tracker.Repository.LoginHistroyRepo;
import com.example.Expense_Tracker.Repository.PasswordResetTokenRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.Repository.PasswordResetOtpRepo;
import com.example.Expense_Tracker.dto.LoginRegister.LoginRequest;
import com.example.Expense_Tracker.dto.LoginRegister.LoginResponse;
import com.example.Expense_Tracker.dto.LoginRegister.RegisterRequest;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpRequest;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpResponse;
import com.example.Expense_Tracker.dto.Password.ForgetPasswordRequest;
import com.example.Expense_Tracker.dto.Password.ResetPasswordRequest;
import com.example.Expense_Tracker.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final LoginHistroyRepo loginHistroyRepo;
    private final PasswordResetOtpRepo passwordResetOtpRepo;
    private final EmailService emailService;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final OtpVerificationService otpVerificationService;


    @Transactional
    public void register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepo.existsByEmail(email)) {
            throw new BadRequestException(
                    "An account with this email already exists."
            );
        }

        Users user = Users.builder()
                .full_name(request.getFullName().trim())
                .email(email)
                .password_hash(passwordEncoder.encode(request.getPassword()))
                .currency("INR")
                .isActive(true)
                .emailVerified(false)
                .build();

        Users savedUser = userRepo.save(user);

        OffsetDateTime now = OffsetDateTime.now();
        String otp = String.format(
                "%06d",
                new SecureRandom().nextInt(1_000_000)
        );

        String otpHash = passwordEncoder.encode(otp);

        PasswordResetOtp verificationOtp = PasswordResetOtp.builder()
                .user(savedUser)
                .purpose(OtpPurpose.EMAIL_VERIFICATION)
                .otpHash(otpHash)
                .createdAt(now)
                .expiresAt(now.plusMinutes(10))
                .attempts(0)
                .verified(false)
                .invalidated(false)
                .build();

        passwordResetOtpRepo.save(verificationOtp);
        emailService.sendEmailVerificationOtp(email, otp);
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase(java.util.Locale.ROOT);

        Users user = userRepo.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword_hash())) {
            throw new InvalidCredentialsException();
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException(
                "Please verify your email before logging in.");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidCredentialsException();
        }

    // Keep the rest of your existing method unchanged.

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        LoginHistory loginHistory = LoginHistory.builder()
                .user(user)
                .loginTime(OffsetDateTime.now())
                .build();
        loginHistroyRepo.save(loginHistory);

        return new LoginResponse(token);
    }

    public VerifyOtpResponse verifyEmail(VerifyOtpRequest request) {
        OtpVerificationResult result = otpVerificationService.verifyEmail(request);

        if (!result.isSuccess()) {
            throw new BadRequestException(result.errorMessage());
        }

        return result.response();
    }

    @Transactional
    public void forgetPassword(ForgetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<Users> usersOptional = userRepo.findByEmailForUpdate(email);

        if (usersOptional.isEmpty()) return;

        Users user = usersOptional.get();
        OffsetDateTime now = OffsetDateTime.now();
        Optional<PasswordResetOtp> latestOtp = passwordResetOtpRepo.findTopByUserOrderByCreatedAtDesc(user);
        if (latestOtp.isPresent()
                && latestOtp.get().getCreatedAt() != null
                && latestOtp.get().getCreatedAt().isAfter(now.minusSeconds(60))) {
            return;
        }

        String otp = String.format("%06d",
                new SecureRandom().nextInt(1_000_000));
        latestOtp.ifPresent(previousOtp -> {
            previousOtp.setInvalidated(true);
            passwordResetOtpRepo.save(previousOtp);
        });

        String otpHash = passwordEncoder.encode(otp);

        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
                .user(user)
                .otpHash(otpHash)
                .expiresAt(now.plusMinutes(10))
                .attempts(0)
                .verified(false)
                .createdAt(now)
                .build();

        passwordResetOtpRepo.save(passwordResetOtp);

        emailService.sendTestMail(user.getEmail(), otp);
    }

    public VerifyOtpResponse verifyOtp(
            com.example.Expense_Tracker.dto.OTP.VerifyOtpRequest request
    ) {
        OtpVerificationResult result = otpVerificationService.verify(request);

        if (!result.isSuccess()) {
            throw new BadRequestException(result.errorMessage());
        }

        return result.response();
    }
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new BadRequestException("New password and confirm password do not match.");

        String email = request.getEmail().trim().toLowerCase();

        Users user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException("Invalid or expired reset token."));

        PasswordResetToken resetToken = passwordResetTokenRepo.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() ->
                        new BadRequestException("Invalid or expired reset token."));

        if (resetToken.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new BadRequestException("Reset token has expired. Please restart the reset process.");

        boolean tokenMatches = passwordEncoder.matches(request.getResetToken(), resetToken.getTokenHash());

        if (!tokenMatches)
            throw new BadRequestException("Invalid or expired reset token.");

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword_hash()))
            throw new BadRequestException("New password must be different from your current password.");

        user.setPassword_hash(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
        resetToken.setUsed(true);

        passwordResetTokenRepo.save(resetToken);
    }
}
