package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Config.JWTService;
import com.example.Expense_Tracker.Repository.LoginHistroyRepo;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.LoginRegister.LoginRequest;
import com.example.Expense_Tracker.dto.LoginRegister.LoginResponse;
import com.example.Expense_Tracker.dto.LoginRegister.RegisterRequest;
import com.example.Expense_Tracker.entity.LoginHistory;
import com.example.Expense_Tracker.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final LoginHistroyRepo loginHistroyRepo;

    public void register(RegisterRequest request){
        if(userRepo.existsByEmail(request.getEmail())){
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

    public LoginResponse login(LoginRequest request){
        Users user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid Password or User_id"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword_hash())){
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
}
