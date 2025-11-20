package com.r2s.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
	void loadUserByUsername_shouldReturnUserDetailsWhenUserExists() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("testuser").password("encodedPassword")
				.fullname("Test User").email("test@example.com")
				.roles(new ArrayList<>(List.of(userRole))).build();

		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

		// Execute
		UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

		// Verify
		assertNotNull(userDetails);
		assertEquals("testuser", userDetails.getUsername());
		assertEquals("encodedPassword", userDetails.getPassword());
		assertEquals(1, userDetails.getAuthorities().size());
		verify(userRepository).findByUsername("testuser");
	}

	@Test
	void loadUserByUsername_shouldThrowExceptionWhenUserNotFound() {
		// Setup
		when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

		// Execute & Verify
		assertThrows(RuntimeException.class, () -> userDetailsService.loadUserByUsername("nonexistent"));
		verify(userRepository).findByUsername("nonexistent");
	}

	@Test
	void loadUserByUsername_shouldHandleUserWithMultipleRoles() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		Role adminRole = Role.builder().id(2).roleName("ADMIN").build();
		User user = User.builder().id(1).username("adminuser").password("password")
				.roles(new ArrayList<>(List.of(userRole, adminRole))).build();

		when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(user));

		// Execute
		UserDetails userDetails = userDetailsService.loadUserByUsername("adminuser");

		// Verify
		assertNotNull(userDetails);
		assertEquals("adminuser", userDetails.getUsername());
		assertEquals(2, userDetails.getAuthorities().size());
		verify(userRepository).findByUsername("adminuser");
	}
}

