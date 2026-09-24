package com.example.Expense_Tracker.dto.UserProfile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String currency;
}
