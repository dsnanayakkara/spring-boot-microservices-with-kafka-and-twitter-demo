package com.microservices.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationController
 * Tests critical authentication endpoints and JWT token generation
 */
@DisplayName("Authentication Controller Tests")
class AuthenticationControllerTest {

    private AuthenticationController authenticationController;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationController = new AuthenticationController(jwtUtil, userDetailsService);

        testUser = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("Should successfully login and return JWT token")
    void shouldSuccessfullyLoginAndReturnJwtToken() {
        // Given
        AuthenticationController.AuthenticationRequest request =
                new AuthenticationController.AuthenticationRequest("testuser", "password");

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn("mock-jwt-token");

        // When
        ResponseEntity<?> response = authenticationController.login(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("mock-jwt-token", body.get("token"));
        assertEquals("Bearer", body.get("type"));
        assertEquals("testuser", body.get("username"));
        assertNotNull(body.get("roles"));

        verify(userDetailsService, times(1)).loadUserByUsername("testuser");
        verify(jwtUtil, times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("Should return error for invalid username")
    void shouldReturnErrorForInvalidUsername() {
        // Given
        AuthenticationController.AuthenticationRequest request =
                new AuthenticationController.AuthenticationRequest("invaliduser", "password");

        when(userDetailsService.loadUserByUsername("invaliduser"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // When
        ResponseEntity<?> response = authenticationController.login(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("Invalid credentials", body.get("error"));

        verify(userDetailsService, times(1)).loadUserByUsername("invaliduser");
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
        // Given
        String validToken = "valid-jwt-token";
        when(jwtUtil.extractUsername(validToken)).thenReturn("testuser");
        when(jwtUtil.validateToken(validToken)).thenReturn(true);

        // When
        ResponseEntity<?> response = authenticationController.validateToken(validToken);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(true, body.get("valid"));
        assertEquals("testuser", body.get("username"));

        verify(jwtUtil, times(1)).extractUsername(validToken);
        verify(jwtUtil, times(1)).validateToken(validToken);
    }

    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtUtil.extractUsername(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

        // When
        ResponseEntity<?> response = authenticationController.validateToken(invalidToken);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(false, body.get("valid"));
    }

    @Test
    @DisplayName("Should handle null username in login request")
    void shouldHandleNullUsernameInLoginRequest() {
        // Given
        AuthenticationController.AuthenticationRequest request =
                new AuthenticationController.AuthenticationRequest(null, "password");

        when(userDetailsService.loadUserByUsername(null))
                .thenThrow(new IllegalArgumentException("Username cannot be null"));

        // When
        ResponseEntity<?> response = authenticationController.login(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should create token with user authorities")
    void shouldCreateTokenWithUserAuthorities() {
        // Given
        UserDetails userWithRoles = User.builder()
                .username("admin")
                .password("password")
                .roles("USER", "ADMIN")
                .build();

        AuthenticationController.AuthenticationRequest request =
                new AuthenticationController.AuthenticationRequest("admin", "password");

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userWithRoles);
        when(jwtUtil.generateToken(userWithRoles)).thenReturn("admin-jwt-token");

        // When
        ResponseEntity<?> response = authenticationController.login(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("admin", body.get("username"));
        assertNotNull(body.get("roles"));
    }

    @Test
    @DisplayName("AuthenticationRequest should have getters and setters")
    void authenticationRequestShouldHaveGettersAndSetters() {
        // Given
        AuthenticationController.AuthenticationRequest request =
                new AuthenticationController.AuthenticationRequest();

        // When
        request.setUsername("testuser");
        request.setPassword("testpassword");

        // Then
        assertEquals("testuser", request.getUsername());
        assertEquals("testpassword", request.getPassword());
    }

    @Test
    @DisplayName("AuthenticationRequest should support constructor initialization")
    void authenticationRequestShouldSupportConstructorInitialization() {
        // Given/When
        AuthenticationController.AuthenticationRequest request =
                new AuthenticationController.AuthenticationRequest("user1", "pass1");

        // Then
        assertEquals("user1", request.getUsername());
        assertEquals("pass1", request.getPassword());
    }

    @Test
    @DisplayName("Should handle empty username")
    void shouldHandleEmptyUsername() {
        // Given
        AuthenticationController.AuthenticationRequest request =
                new AuthenticationController.AuthenticationRequest("", "password");

        when(userDetailsService.loadUserByUsername(""))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // When
        ResponseEntity<?> response = authenticationController.login(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should validate token and return false for null token")
    void shouldValidateTokenAndReturnFalseForNullToken() {
        // Given
        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("Null token"));

        // When
        ResponseEntity<?> response = authenticationController.validateToken("null-token");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(false, body.get("valid"));
    }
}
