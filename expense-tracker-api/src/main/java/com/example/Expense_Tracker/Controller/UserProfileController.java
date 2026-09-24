package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Service.UserService;
import com.example.Expense_Tracker.dto.Password.ChangePasswordRequest;
import com.example.Expense_Tracker.dto.UserProfile.UserProfileRequest;
import com.example.Expense_Tracker.dto.UserProfile.UserProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class UserProfileController {
    private  final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(){
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@Valid @RequestBody UserProfileRequest request){
        return ResponseEntity.ok(userService.updateCurrentProfile(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String>  ChangePassword(@Valid @RequestBody ChangePasswordRequest request){
        userService.changePassword(request);
        return ResponseEntity.ok("Password Changed SuccessFully");
    }
}
