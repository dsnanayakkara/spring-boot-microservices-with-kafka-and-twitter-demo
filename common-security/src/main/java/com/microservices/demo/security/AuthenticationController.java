package com.microservices.demo.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication REST Controller
 * Provides JWT token generation endpoint
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthenticationController(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Login endpoint - Returns JWT token
     * POST /api/v1/auth/login
     * Body: {"username": "user", "password": "password"}
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            // In production, verify password with AuthenticationManager
            // For demo, we'll generate token if user exists

            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("username", userDetails.getUsername());
            response.put("roles", userDetails.getAuthorities());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    /**
     * Token validation endpoint
     * GET /api/v1/auth/validate?token=xxx
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            boolean isValid = jwtUtil.validateToken(token);

            return ResponseEntity.ok(Map.of(
                    "valid", isValid,
                    "username", username != null ? username : "N/A"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    /**
     * Request body for login
     */
    public static class AuthenticationRequest {
        private String username;
        private String password;

        public AuthenticationRequest() {}

        public AuthenticationRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
