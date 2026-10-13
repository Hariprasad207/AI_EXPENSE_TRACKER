package com.example.Expense_Tracker.Config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JWTServiceTest {
    private JWTService jwtService;
    private final String secret = "this-is-a-very-long-secret-key-for-testing-jwt-security-123456789";
    private final long expirationTime = 3600000L;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService(secret, expirationTime);
    }

    @Test
    void generateToken_shouldCreateValidToken() {

        String token = jwtService.generateToken(1L,"test@example.com");

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void extractUserId_shouldReturnCorrectUserId() {
        String token = jwtService.generateToken(123L,"test@example.com");
        Long userId = jwtService.extractUserId(token);

        assertEquals(123L, userId);
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.jwt.token";

        assertFalse(jwtService.isTokenValid(invalidToken));
    }

    @Test
    void isTokenValid_shouldReturnFalseForModifiedToken() {
        String token = jwtService.generateToken(1L,"test@example.com");
        String modifiedToken = token + "modified";

        assertFalse(jwtService.isTokenValid(modifiedToken));
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() throws InterruptedException {
        JWTService shortLivedJwtService =new JWTService(secret, 1);

        String token = shortLivedJwtService.generateToken(1L,"test@example.com");
        Thread.sleep(20);

        assertFalse(shortLivedJwtService.isTokenValid(token));
    }
}