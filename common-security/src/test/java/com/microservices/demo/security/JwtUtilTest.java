package com.microservices.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil
 * Tests critical JWT token generation and validation logic
 */
@DisplayName("JWT Utility Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails testUser;
    private static final String TEST_SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm-to-work-properly";
    private static final Long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);

        testUser = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidToken() {
        // When
        String token = jwtUtil.generateToken(testUser);

        // Then
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
        assertTrue(token.split("\\.").length == 3, "Token should have 3 parts (header.payload.signature)");
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsername() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals("testuser", extractedUsername, "Extracted username should match");
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateTokenSuccessfully() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean isValid = jwtUtil.validateToken(token, "testuser");

        // Then
        assertTrue(isValid, "Token should be valid");
    }

    @Test
    @DisplayName("Should reject token with wrong username")
    void shouldRejectTokenWithWrongUsername() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean isValid = jwtUtil.validateToken(token, "wronguser");

        // Then
        assertFalse(isValid, "Token should be invalid for different username");
    }

    @Test
    @DisplayName("Should validate token without username parameter")
    void shouldValidateTokenWithoutUsername() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid, "Token should be valid");
    }

    @Test
    @DisplayName("Should reject malformed token")
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "invalid.token.here";

        // When
        Boolean isValid = jwtUtil.validateToken(malformedToken);

        // Then
        assertFalse(isValid, "Malformed token should be invalid");
    }

    @Test
    @DisplayName("Should generate token with string username")
    void shouldGenerateTokenWithStringUsername() {
        // When
        String token = jwtUtil.generateToken("testuser");

        // Then
        assertNotNull(token, "Token should not be null");
        assertEquals("testuser", jwtUtil.extractUsername(token), "Username should match");
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpirationDate() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        var expirationDate = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expirationDate, "Expiration date should not be null");
        assertTrue(expirationDate.getTime() > System.currentTimeMillis(), "Expiration should be in the future");
    }

    @Test
    @DisplayName("Should create different tokens for different users")
    void shouldCreateDifferentTokensForDifferentUsers() {
        // Given
        UserDetails user1 = User.builder()
                .username("user1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        UserDetails user2 = User.builder()
                .username("user2")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Then
        assertNotEquals(token1, token2, "Tokens should be different for different users");
        assertEquals("user1", jwtUtil.extractUsername(token1));
        assertEquals("user2", jwtUtil.extractUsername(token2));
    }

    @Test
    @DisplayName("Should handle null token gracefully")
    void shouldHandleNullToken() {
        // When/Then
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(null),
                "Should throw exception for null token");
    }

    @Test
    @DisplayName("Should handle empty token gracefully")
    void shouldHandleEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        Boolean isValid = jwtUtil.validateToken(emptyToken);

        // Then
        assertFalse(isValid, "Empty token should be invalid");
    }
}
