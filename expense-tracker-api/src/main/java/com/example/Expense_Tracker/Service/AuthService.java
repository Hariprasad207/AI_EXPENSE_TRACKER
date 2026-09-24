package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Config.JWTService;
import com.example.Expense_Tracker.Exception.BadRequestException;
import com.example.Expense_Tracker.Repository.LoginHistroyRepo;
import com.example.Expense_Tracker.Repository.PasswordResetTokenRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.Repository.PasswordResetOtpRepo;
import com.example.Expense_Tracker.dto.LoginRegister.LoginRequest;
import com.example.Expense_Tracker.dto.LoginRegister.LoginResponse;
import com.example.Expense_Tracker.dto.LoginRegister.RegisterRequest;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpResponse;
import com.example.Expense_Tracker.dto.Password.ForgetPasswordRequest;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpRequest;
import com.example.Expense_Tracker.dto.Password.ResetPasswordRequest;
import com.example.Expense_Tracker.entity.LoginHistory;
import com.example.Expense_Tracker.entity.PasswordResetToken;
import com.example.Expense_Tracker.entity.Users;
import com.example.Expense_Tracker.entity.PasswordResetOtp;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
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

    public void register(RegisterRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("You are already Registered");
        }
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        Users user = Users.builder()
                .full_name(request.getFullName())
                .email(request.getEmail())
                .password_hash(hashedPassword)
                .currency("INR")
                .isActive(true)
                .build();

        userRepo.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        Users user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid Password or User_id"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword_hash())) {
            throw new RuntimeException("Invalid Data");
        }
        String token = jwtService.generateToken((long) user.getId(), user.getEmail());
        LoginHistory loginHistory = LoginHistory.builder()
                .user(user)
                .loginTime(OffsetDateTime.now())
                .build();
        loginHistroyRepo.save(loginHistory);

        return new LoginResponse(token);
    }

    @Transactional
    public void forgetPassword(ForgetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<Users> usersOptional = userRepo.findByEmail(email);

        if (usersOptional.isEmpty()) return;

        Users user = usersOptional.get();

        String otp = String.format("%06d",
                new SecureRandom().nextInt(1_000_000));

        passwordResetOtpRepo.findTopByUserOrderByCreatedAtDesc(user)
                .ifPresent(previousOtp -> {
                    previousOtp.setVerified(true);
                    passwordResetOtpRepo.save(previousOtp);
                });

        String otpHash = passwordEncoder.encode(otp);

        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
                .user(user)
                .otpHash(otpHash)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .attempts(0)
                .verified(false)
                .createdAt(OffsetDateTime.now())
                .build();

        passwordResetOtpRepo.save(passwordResetOtp);

        emailService.sendTestMail(user.getEmail(), otp);
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(com.example.Expense_Tracker.dto.OTP.VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("The user with that email id not found"));

        PasswordResetOtp resetOtp = passwordResetOtpRepo
                .findTopByUserAndInvalidatedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("No active OTP found.Please request a new OTP"));

        if (Boolean.TRUE.equals(resetOtp.getVerified()))
            throw new BadRequestException("This Otp is already verified.Please request a new OTP");

        if (resetOtp.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new BadRequestException("OTP is expired. Request a new OTP");

        if (resetOtp.getAttempts() >= 5)
            throw new BadRequestException("Tried Too much time. Request a new OTP");

        if (Boolean.TRUE.equals(resetOtp.getInvalidated()))
            throw new BadRequestException("This OTP is no longer valid. Please request a new OTP.");

        boolean otpMatches = passwordEncoder.matches(request.getOtp(), resetOtp.getOtpHash());

        if (!otpMatches) {
            resetOtp.setAttempts(resetOtp.getAttempts() + 1);
            passwordResetOtpRepo.save(resetOtp);
            int remainingAttempts = 5 - resetOtp.getAttempts();
            if (remainingAttempts <= 0)
                throw new BadRequestException("Too many incorrect attempts. Please request a new OTP.");

            throw new BadRequestException("Invalid OTP. " + remainingAttempts + " attempts remaining.");
        }
        resetOtp.setVerified(true);
        passwordResetOtpRepo.save(resetOtp);

        passwordResetTokenRepo.findLatestUnusedToken(user)
                .ifPresent(previousToken -> {
                    previousToken.setUsed(true);
                    passwordResetTokenRepo.save(previousToken);
                });

        String rawResetToken = UUID.randomUUID().toString();

        String tokenHash = passwordEncoder.encode(rawResetToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .used(false)
                .createdAt(OffsetDateTime.now())
                .build();

        passwordResetTokenRepo.save(resetToken);

        return VerifyOtpResponse.builder()
                .message("OTP VERIFIED SUCCESSFULLY")
                .resetToken(rawResetToken)
                .build();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new BadRequestException("New password and confirm password do not match.");

        String email = request.getEmail().trim().toLowerCase();

        Users user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException("Invalid or expired reset token."));

        PasswordResetToken resetToken =passwordResetTokenRepo.findLatestUnusedToken(user)
                        .orElseThrow(() ->
                                new BadRequestException("Invalid or expired reset token."));

        if (resetToken.getExpiresAt().isBefore(OffsetDateTime.now()))
            throw new BadRequestException("Reset token has expired. Please restart the reset process.");

        boolean tokenMatches = passwordEncoder.matches(request.getResetToken(),resetToken.getTokenHash());

        if (!tokenMatches)
            throw new BadRequestException("Invalid or expired reset token.");

        if (passwordEncoder.matches(request.getNewPassword(),user.getPassword_hash()))
            throw new BadRequestException("New password must be different from your current password.");

        user.setPassword_hash(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
        resetToken.setUsed(true);

        passwordResetTokenRepo.save(resetToken);
    }
}