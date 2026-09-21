package com.example.Expense_Tracker.Config;

import com.example.Expense_Tracker.entity.Users;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public Users getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication==null || !authentication.isAuthenticated()){
            throw new RuntimeException("User is not authenticate");
        }
        return (Users) authentication.getPrincipal();
    }

    public Long getCurrentUserID(){
        return (long) getCurrentUser().getId();
    }
}
