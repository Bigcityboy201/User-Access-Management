package com.r2s.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("loadUserByUsername should return UserDetails when user exists")
    void loadUserByUsername_shouldReturnUserDetailsWhenUserExists() {
        // ===== ARRANGE =====
        Role userRole = Role.builder().id(1).roleName("USER").build();
        User user = User.builder().id(1).username("testuser").password("encodedPassword")
                .fullname("Test User").email("test@example.com")
                .roles(new ArrayList<>(List.of(userRole))).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // ===== ACT =====
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // ===== ASSERT =====
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("loadUserByUsername should throw exception when user not found")
    void loadUserByUsername_shouldThrowExceptionWhenUserNotFound() {
        // ===== ARRANGE =====
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // ===== ACT & ASSERT =====
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent"));
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("loadUserByUsername should handle user with multiple roles")
    void loadUserByUsername_shouldHandleUserWithMultipleRoles() {
        // ===== ARRANGE =====
        Role userRole = Role.builder().id(1).roleName("USER").build();
        Role adminRole = Role.builder().id(2).roleName("ADMIN").build();
        User user = User.builder().id(1).username("adminuser").password("password")
                .roles(new ArrayList<>(List.of(userRole, adminRole))).build();

        when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(user));

        // ===== ACT =====
        UserDetails userDetails = userDetailsService.loadUserByUsername("adminuser");

        // ===== ASSERT =====
        assertNotNull(userDetails);
        assertEquals("adminuser", userDetails.getUsername());
        assertEquals(2, userDetails.getAuthorities().size());
        verify(userRepository).findByUsername("adminuser");
    }

    @Test
    @DisplayName("loadUserByUsername should throw exception when user is marked deleted")
    void loadUserByUsername_shouldThrowExceptionWhenUserDeleted() {
        // ===== ARRANGE =====
        Role userRole = Role.builder().id(1).roleName("USER").build();
        User deletedUser = User.builder().id(1).username("deleteduser").password("password")
                .deleted(true)
                .roles(new ArrayList<>(List.of(userRole))).build();

        when(userRepository.findByUsername("deleteduser")).thenReturn(Optional.of(deletedUser));

        // ===== ACT & ASSERT =====
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("deleteduser"));
        verify(userRepository).findByUsername("deleteduser");
    }

    @Test
    @DisplayName("loadUserByUsername should handle user with no roles")
    void loadUserByUsername_shouldHandleUserWithNoRoles() {
        // ===== ARRANGE =====
        User user = User.builder().id(1).username("norolesuser").password("password")
                .roles(new ArrayList<>()).build();

        when(userRepository.findByUsername("norolesuser")).thenReturn(Optional.of(user));

        // ===== ACT =====
        UserDetails userDetails = userDetailsService.loadUserByUsername("norolesuser");

        // ===== ASSERT =====
        assertNotNull(userDetails);
        assertEquals("norolesuser", userDetails.getUsername());
        assertEquals(0, userDetails.getAuthorities().size());
        verify(userRepository).findByUsername("norolesuser");
    }

    @Test
    @DisplayName("loadUserByUsername should throw exception when username is null")
    void loadUserByUsername_shouldHandleNullUsername() {
        // ===== ARRANGE =====
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // ===== ACT & ASSERT =====
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(null));
        verify(userRepository).findByUsername(null);
    }
}
