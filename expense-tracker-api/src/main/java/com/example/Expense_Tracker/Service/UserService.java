package com.example.Expense_Tracker.Service;

import com.example.Expense_Tracker.Config.CurrentUserService;
import com.example.Expense_Tracker.Exception.BadRequestException;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.dto.Password.ChangePasswordRequest;
import com.example.Expense_Tracker.dto.UserProfile.UserProfileRequest;
import com.example.Expense_Tracker.dto.UserProfile.UserProfileResponse;
import com.example.Expense_Tracker.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    private UserProfileResponse mapToResponse(Users user){
        return UserProfileResponse.builder()
                .id((long)user.getId())
                .fullName(user.getFull_name())
                .email(user.getEmail())
                .currency(user.getCurrency())
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(){
        Users user = currentUserService.getCurrentUser();
        return mapToResponse(user);
    }

    @Transactional
    public UserProfileResponse updateCurrentProfile(UserProfileRequest request){
        Users user = currentUserService.getCurrentUser();
        if(!user.getEmail().equalsIgnoreCase(request.getEmail())){
            if(userRepo.existsByEmail(request.getEmail()))
                    throw new RuntimeException("Email is already registered by another user");
            user.setEmail(request.getEmail());
        }
        user.setFull_name(request.getFullName());
        user.setCurrency(request.getCurrency());

        Users updatedUser = userRepo.save(user);

        return mapToResponse(updatedUser);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        Users user = currentUserService.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(),user.getPassword_hash()))
            throw new BadRequestException(
                    "It is not matching with the Previous Password"
            );

        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new BadRequestException(
                    "The passwords does not Matched.."
            );

        if (passwordEncoder.matches(request.getNewPassword(),user.getPassword_hash()))
            throw new BadRequestException("New password must be differ from the Previous password");

        String hashedPassword =passwordEncoder.encode(request.getNewPassword());
        user.setPassword_hash(hashedPassword);
        userRepo.save(user);
    }
}
