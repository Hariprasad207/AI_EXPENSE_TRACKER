package com.example.Expense_Tracker.Config;

import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityIntegrationTest.TestController.class)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserRepo userRepo;

    @BeforeEach
    void setUp() {
        when(jwtService.isTokenValid("invalid-token"))
                .thenReturn(false);
    }

    @Test
    void protectedEndpoint_shouldReturnUnauthorized_whenNoTokenProvided()
            throws Exception {

        mockMvc.perform(
                        get("/test/protected")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldReturnUnauthorized_whenInvalidTokenProvided()
            throws Exception {

        mockMvc.perform(
                        get("/test/protected")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldAllowRequest_whenValidTokenAndActiveUser()
            throws Exception {

        Long userId = 1L;
        String token = "valid-token";

        Users user = Users.builder()
                .id(userId)
                .full_name("Test User")
                .email("test@example.com")
                .password_hash("hashed-password")
                .currency("INR")
                .isActive(true)
                .build();

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractUserId(token))
                .thenReturn(userId);

        when(userRepo.findById(userId))
                .thenReturn(Optional.of(user));

        mockMvc.perform(
                        get("/test/protected")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_shouldReturnUnauthorized_whenUserIsInactive()
            throws Exception {

        Long userId = 2L;
        String token = "inactive-user-token";

        Users user = Users.builder()
                .id(userId)
                .full_name("Inactive User")
                .email("inactive@example.com")
                .password_hash("hashed-password")
                .currency("INR")
                .isActive(false)
                .build();

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractUserId(token))
                .thenReturn(userId);

        when(userRepo.findById(userId))
                .thenReturn(Optional.of(user));

        mockMvc.perform(
                        get("/test/protected")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @RestController
    static class TestController {

        @GetMapping("/test/protected")
        public String protectedEndpoint() {
            return "protected";
        }

        @GetMapping("/test/public")
        public String publicEndpoint() {
            return "public";
        }
    }
}