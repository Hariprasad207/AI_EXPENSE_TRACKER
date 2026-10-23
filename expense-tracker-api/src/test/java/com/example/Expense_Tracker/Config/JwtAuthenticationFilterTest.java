package com.example.Expense_Tracker.Config;

import com.example.Expense_Tracker.Repository.UserRepo;
import com.example.Expense_Tracker.entity.Users;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JWTService jwtService;
    private UserRepo userRepo;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtService = mock(JWTService.class);
        userRepo = mock(UserRepo.class);
        filterChain = mock(FilterChain.class);
        jwtAuthenticationFilter =new JwtAuthenticationFilter(jwtService, userRepo);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_shouldContinueWithoutAuthentication_whenAuthorizationHeaderIsMissing()
            throws ServletException, IOException {

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepo);
    }

    @Test
    void doFilter_shouldContinueWithoutAuthentication_whenAuthorizationHeaderIsNotBearer()
            throws ServletException, IOException {

        request.addHeader("Authorization", "Basic some-token");
        jwtAuthenticationFilter.doFilter(request, response, filterChain);
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userRepo);
    }

    @Test
    void doFilter_shouldContinueWithoutAuthentication_whenTokenIsInvalid()
            throws ServletException, IOException {

        String token = "invalid-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).isTokenValid(token);
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(userRepo);
    }

    @Test
    void doFilter_shouldAuthenticateUser_whenTokenIsValidAndUserIsActive()
            throws ServletException, IOException {

        String token = "valid-token";
        Long userId = 1L;

        Users user = Users.builder()
                .id(userId)
                .full_name("Test User")
                .email("test@example.com")
                .password_hash("hashed-password")
                .currency("INR")
                .isActive(true)
                .build();

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractUserId(token))
                .thenReturn(userId);

        when(userRepo.findById(userId))
                .thenReturn(Optional.of(user));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).isTokenValid(token);
        verify(jwtService).extractUserId(token);
        verify(userRepo).findById(userId);
        verify(filterChain).doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(user, authentication.getPrincipal());
        assertEquals(0, authentication.getAuthorities().size());
    }

    @Test
    void doFilter_shouldNotAuthenticate_whenUserDoesNotExist()
            throws ServletException, IOException {

        String token = "valid-token";
        Long userId = 99L;

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractUserId(token))
                .thenReturn(userId);

        when(userRepo.findById(userId))
                .thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).isTokenValid(token);
        verify(jwtService).extractUserId(token);
        verify(userRepo).findById(userId);
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_shouldNotAuthenticate_whenUserIsInactive()
            throws ServletException, IOException {

        String token = "valid-token";
        Long userId = 2L;

        Users user = Users.builder()
                .id(userId)
                .full_name("Inactive User")
                .email("inactive@example.com")
                .password_hash("hashed-password")
                .currency("INR")
                .isActive(false)
                .build();

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractUserId(token))
                .thenReturn(userId);

        when(userRepo.findById(userId))
                .thenReturn(Optional.of(user));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).isTokenValid(token);
        verify(jwtService).extractUserId(token);
        verify(userRepo).findById(userId);
        verify(filterChain).doFilter(request, response);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }


    @Test
    void doFilter_shouldContinueWithoutAuthentication_whenExtractUserIdThrowsJwtException()
            throws ServletException, IOException {

        String token = "malformed-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserId(token))
                .thenThrow(new io.jsonwebtoken.MalformedJwtException("Malformed token"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).isTokenValid(token);
        verify(jwtService).extractUserId(token);
        verifyNoInteractions(userRepo);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_shouldContinueWithoutAuthentication_whenExtractUserIdThrowsIllegalArgumentException()
            throws ServletException, IOException {

        String token = "invalid-subject-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUserId(token))
                .thenThrow(new IllegalArgumentException("Invalid user ID"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).isTokenValid(token);
        verify(jwtService).extractUserId(token);
        verifyNoInteractions(userRepo);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

}
