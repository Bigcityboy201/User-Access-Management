package com.r2s.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long TEST_DURATION = 86400L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtDuration", TEST_DURATION);
    }

    @Test
    @DisplayName("generateToken should create a valid token")
    void generateToken_shouldCreateValidToken() {
        // ===== ARRANGE =====
        Role userRole = Role.builder().id(1).roleName("USER").build();
        User user = User.builder().id(1).username("testuser").password("password")
                .roles(List.of(userRole)).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // ===== ACT =====
        String token = jwtUtils.generateToken(userDetails);

        // ===== ASSERT =====
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("extractUsername should extract username from token")
    void extractUsername_shouldExtractUsernameFromToken() {
        // ===== ARRANGE =====
        Role userRole = Role.builder().id(1).roleName("USER").build();
        User user = User.builder().id(1).username("testuser").password("password")
                .roles(List.of(userRole)).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtils.generateToken(userDetails);

        // ===== ACT =====
        String username = jwtUtils.extractUsername(token);

        // ===== ASSERT =====
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("extractUsername should throw exception for invalid token")
    void extractUsername_shouldThrowExceptionForInvalidToken() {
        // ===== ARRANGE =====
        String invalidToken = "invalid.token";

        // ===== ACT & ASSERT =====
        assertThrows(Exception.class, () -> jwtUtils.extractUsername(invalidToken));
    }

    @Test
    @DisplayName("extractExpiration should extract expiration date")
    void extractExpiration_shouldExtractExpirationDate() {
        // ===== ARRANGE =====
        Role userRole = Role.builder().id(1).roleName("USER").build();
        User user = User.builder().id(1).username("testuser").password("password")
                .roles(List.of(userRole)).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtils.generateToken(userDetails);

        // ===== ACT =====
        Date expiration = jwtUtils.extractExpiration(token);

        // ===== ASSERT =====
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("isTokenExpired should return false for valid token")
    void isTokenExpired_shouldReturnFalseForValidToken() {
        // ===== ARRANGE =====
        Role userRole = Role.builder().id(1).roleName("USER").build();
        User user = User.builder().id(1).username("testuser").password("password")
                .roles(List.of(userRole)).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtils.generateToken(userDetails);

        // ===== ACT =====
        boolean expired = jwtUtils.isTokenExpired(token);

        // ===== ASSERT =====
        assertFalse(expired);
    }

    @Test
    @DisplayName("validateToken should return true for valid token")
    void validateToken_shouldReturnTrueForValidToken() {
        // ===== ARRANGE =====
        Role userRole = Role.builder().id(1).roleName("USER").build();
        User user = User.builder().id(1).username("testuser").password("password")
                .roles(List.of(userRole)).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtils.generateToken(userDetails);

        // ===== ACT =====
        boolean valid = jwtUtils.validateToken(token);

        // ===== ASSERT =====
        assertTrue(valid);
    }

    @Test
    @DisplayName("validateToken should return false for invalid token")
    void validateToken_shouldReturnFalseForInvalidToken() {
        // ===== ARRANGE =====
        String invalidToken = "invalid.token.here";

        // ===== ACT =====
        boolean valid = jwtUtils.validateToken(invalidToken);

        // ===== ASSERT =====
        assertFalse(valid);
    }

    @Test
    @DisplayName("validateToken should return false for expired token")
    void validateToken_shouldReturnFalseForExpiredToken() {
        // ===== ARRANGE =====
        JwtUtils shortLivedJwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(shortLivedJwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(shortLivedJwtUtils, "jwtDuration", -3600L);

        Role userRole = Role.builder().id(1).roleName("USER").build();
        User user = User.builder().id(1).username("testuser").password("password")
                .roles(List.of(userRole)).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String expiredToken = shortLivedJwtUtils.generateToken(userDetails);

        // Wait a bit to ensure expiration
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ===== ACT =====
        boolean valid = jwtUtils.validateToken(expiredToken);

        // ===== ASSERT =====
        assertFalse(valid);
    }

    @Test
    @DisplayName("validateToken should return false for null token")
    void validateToken_shouldReturnFalseForNullToken() {
        // ===== ACT =====
        boolean valid = jwtUtils.validateToken(null);

        // ===== ASSERT =====
        assertFalse(valid);
    }

    @Test
    @DisplayName("validateToken should return false for empty token")
    void validateToken_shouldReturnFalseForEmptyToken() {
        // ===== ACT =====
        boolean valid = jwtUtils.validateToken("");

        // ===== ASSERT =====
        assertFalse(valid);
    }
}
