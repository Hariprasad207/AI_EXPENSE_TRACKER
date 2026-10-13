
package com.example.Expense_Tracker.Controller;

import com.example.Expense_Tracker.Config.JWTService;
import com.example.Expense_Tracker.Config.TestSecurityConfig;
import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.Service.AuthService;
import com.example.Expense_Tracker.dto.LoginRegister.LoginResponse;
import com.example.Expense_Tracker.dto.OTP.VerifyOtpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@Import(TestSecurityConfig.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepo userRepo;

    @Test
    void register_shouldReturnCreatedForValidRequest() throws Exception {
        String request = """
                {
                    "fullName": "Hari",
                    "email": "hari@example.com",
                    "password": "password@123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(content().string("User Registered Successfully"));

        verify(authService).register(any());
    }

    @Test
    void register_shouldReturnBadRequestForInvalidRequest()
            throws Exception {
        String request = """
                {
                    "fullName": "",
                    "email": "invalid-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void login_shouldReturnOkForValidRequest() throws Exception {
        String request = """
                {
                    "email": "hari@example.com",
                    "password": "password@123"
                }
                """;

        LoginResponse response = new LoginResponse("jwt-token");

        when(authService.login(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).login(any());
    }

    @Test
    void login_shouldReturnBadRequestForInvalidRequest()
            throws Exception {
        String request = """
                {
                    "email": "invalid-email",
                    "password": ""
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void forgotPassword_shouldReturnOk() throws Exception {

        String request = """
                {
                    "email": "hari@example.com"
                }
                """;

        doNothing()
                .when(authService)
                .forgetPassword(any());

        mockMvc.perform(
                        post("/api/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "If an account exists for this email, "
                                + "a verification code has been sent."
                ));

        verify(authService).forgetPassword(any());
    }

    @Test
    void verifyOtp_shouldReturnOk() throws Exception {

        String request = """
                {
                    "email": "hari@example.com",
                    "otp": "123456"
                }
                """;

        VerifyOtpResponse response =
                VerifyOtpResponse.builder()
                        .message("OTP VERIFIED SUCCESSFULLY")
                        .resetToken("reset-token")
                        .build();

        when(authService.verifyOtp(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/verify-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("OTP VERIFIED SUCCESSFULLY"))
                .andExpect(jsonPath("$.resetToken")
                        .value("reset-token"));

        verify(authService).verifyOtp(any());
    }

    @Test
    void resetPassword_shouldReturnOk() throws Exception {

        String request = """
                {
                    "email": "hari@example.com",
                    "resetToken": "reset-token",
                    "newPassword": "NewPassword@123",
                    "confirmPassword": "NewPassword@123"
                }
                """;

        doNothing()
                .when(authService)
                .resetPassword(any());

        mockMvc.perform(
                        post("/api/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Password reset successfully. "
                                + "You can now login with your new password."
                ));

        verify(authService).resetPassword(any());
    }
}
