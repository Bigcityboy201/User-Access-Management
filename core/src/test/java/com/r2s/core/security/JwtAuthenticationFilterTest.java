package com.r2s.core.security;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.util.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal - Should set authentication when valid token")
    void doFilterInternal_shouldSetAuthentication_whenValidToken() throws ServletException, IOException {
        // ===== ARRANGE =====
        String token = "valid-token";
        request.addHeader("Authorization", "Bearer " + token);

        Role userRole = Role.builder().id(1).roleName("USER").build();
        User user = User.builder().id(1).username("testuser").password("password")
                .roles(new ArrayList<>(List.of(userRole))).build();
        UserDetails userDetails = new CustomUserDetails(user);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.extractUsername(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // ===== ACT =====
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ===== ASSERT =====
        verify(jwtUtils).validateToken(token);
        verify(jwtUtils).extractUsername(token);
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Should not set authentication when no token")
    void doFilterInternal_shouldNotSetAuthentication_whenNoToken() throws ServletException, IOException {
        // ===== ACT =====
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ===== ASSERT =====
        verify(jwtUtils, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal - Should not set authentication when invalid token")
    void doFilterInternal_shouldNotSetAuthentication_whenInvalidToken() throws ServletException, IOException {
        // ===== ARRANGE =====
        String token = "invalid-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(false);

        // ===== ACT =====
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ===== ASSERT =====
        verify(jwtUtils).validateToken(token);
        verify(jwtUtils, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal - Should not set authentication when token without Bearer prefix")
    void doFilterInternal_shouldNotSetAuthentication_whenTokenWithoutBearerPrefix() throws ServletException, IOException {
        // ===== ARRANGE =====
        request.addHeader("Authorization", "invalid-token");

        // ===== ACT =====
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ===== ASSERT =====
        verify(jwtUtils, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - Should handle expired token gracefully")
    void doFilterInternal_shouldHandleExpiredToken_whenTokenExpired() throws ServletException, IOException {
        // ===== ARRANGE =====
        String token = "expired-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenThrow(new RuntimeException("Token expired"));

        // ===== ACT =====
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ===== ASSERT =====
        verify(jwtUtils).validateToken(token);
        verify(jwtUtils, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("doFilterInternal - Should handle missing user in database gracefully")
    void doFilterInternal_shouldHandleUserNotFoundInDatabase_whenUserMissing() throws ServletException, IOException {
        // ===== ARRANGE =====
        String token = "valid-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.extractUsername(token)).thenReturn("missingUser");
        when(userDetailsService.loadUserByUsername("missingUser")).thenThrow(new RuntimeException("User not found"));

        // ===== ACT =====
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // ===== ASSERT =====
        verify(jwtUtils).validateToken(token);
        verify(jwtUtils).extractUsername(token);
        verify(userDetailsService).loadUserByUsername("missingUser");
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
