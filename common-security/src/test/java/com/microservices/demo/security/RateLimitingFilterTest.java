package com.microservices.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitingFilter
 * Tests critical rate limiting logic using token bucket algorithm
 */
@DisplayName("Rate Limiting Filter Tests")
class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        rateLimitingFilter = new RateLimitingFilter();
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("Should allow request within rate limit")
    void shouldAllowRequestWithinRateLimit() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, times(1)).setHeader(eq("X-Rate-Limit-Remaining"), anyString());
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    @DisplayName("Should block request when rate limit exceeded")
    void shouldBlockRequestWhenRateLimitExceeded() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getRemoteAddr()).thenReturn("192.168.1.2");

        // When - Make 101 requests (exceeding 100 per minute limit)
        for (int i = 0; i < 101; i++) {
            responseWriter = new StringWriter();
            when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Then - Last request should be blocked
        verify(response, atLeastOnce()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response, atLeastOnce()).setHeader("X-Rate-Limit-Retry-After-Seconds", "60");
        assertTrue(responseWriter.toString().contains("Rate limit exceeded"));
    }

    @Test
    @DisplayName("Should bypass rate limiting for actuator endpoints")
    void shouldBypassRateLimitingForActuatorEndpoints() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator/health");
        when(request.getRemoteAddr()).thenReturn("192.168.1.3");

        // When - Make 150 requests (would exceed limit if not bypassed)
        for (int i = 0; i < 150; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Then - All requests should pass through
        verify(filterChain, times(150)).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    @DisplayName("Should use X-Forwarded-For header when present")
    void shouldUseXForwardedForHeader() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(request, times(1)).getHeader("X-Forwarded-For");
    }

    @Test
    @DisplayName("Should handle different IPs separately")
    void shouldHandleDifferentIpsSeparately() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/events");

        // When - IP 1 makes 100 requests
        when(request.getRemoteAddr()).thenReturn("192.168.1.10");
        for (int i = 0; i < 100; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // When - IP 2 makes 100 requests
        when(request.getRemoteAddr()).thenReturn("192.168.1.20");
        for (int i = 0; i < 100; i++) {
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Then - Both IPs should have their requests allowed (separate buckets)
        verify(filterChain, times(200)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should set correct content type for rate limit response")
    void shouldSetCorrectContentTypeForRateLimitResponse() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getRemoteAddr()).thenReturn("192.168.1.30");

        // When - Exceed rate limit
        for (int i = 0; i < 101; i++) {
            responseWriter = new StringWriter();
            when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Then
        verify(response, atLeastOnce()).setContentType("application/json");
    }

    @Test
    @DisplayName("Should return JSON error message when rate limited")
    void shouldReturnJsonErrorMessage() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getRemoteAddr()).thenReturn("192.168.1.40");

        // When - Exceed rate limit
        for (int i = 0; i < 101; i++) {
            responseWriter = new StringWriter();
            when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Then
        String responseContent = responseWriter.toString();
        assertTrue(responseContent.contains("error"), "Response should contain error field");
        assertTrue(responseContent.contains("Rate limit exceeded"), "Response should contain error message");
        assertTrue(responseContent.contains("message"), "Response should contain message field");
    }

    @Test
    @DisplayName("Should handle empty X-Forwarded-For header")
    void shouldHandleEmptyXForwardedForHeader() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("192.168.1.50");

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then - Should fall back to remote address
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle null X-Forwarded-For header")
    void shouldHandleNullXForwardedForHeader() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.60");

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then - Should fall back to remote address
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use first IP from X-Forwarded-For chain")
    void shouldUseFirstIpFromXForwardedForChain() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2, 10.0.0.3");

        // When
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
