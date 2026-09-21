package com.example.Expense_Tracker.dto.LoginRegister;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Required name")
    @Size(min = 2,max = 100, message = "You only have 50 characters")
    private String fullName;

    @NotBlank(message = "Needed to enter the mail_id")
    @Email(message = "Please Provide the valid Mail_id")
    @Size(max = 100, message = "You mail id must be below 100character")
    private String email;

    @NotBlank(message = "Needed the Password")
    @Size(min = 8,max = 50,message = "You password must need to contain 8 to 50 characters")
    private String password;
}
