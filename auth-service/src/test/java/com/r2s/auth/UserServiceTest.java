package com.r2s.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.r2s.auth.kafka.UserKafkaProducer;
import com.r2s.auth.service.impl.UserServiceIMPL;
import com.r2s.core.constant.SecurityRole;
import com.r2s.core.dto.CreateUserProfileDTO;
import com.r2s.core.dto.request.SignInRequest;
import com.r2s.core.dto.request.SignUpRequest;
import com.r2s.core.dto.response.SignInResponse;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.exception.UserAlreadyExistException;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.security.CustomUserDetails;
import com.r2s.core.util.JwtUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private UserKafkaProducer producer;

	@InjectMocks
	private UserServiceIMPL userService;

	private static final long JWT_DURATION = 86400L; // 1 day in seconds

	@BeforeEach
	void setUp() {
		// Set jwtDuration field using ReflectionTestUtils
		ReflectionTestUtils.setField(userService, "jwtDuration", JWT_DURATION);
	}

	// === TEST signUp() - success ===
	@Test
	void signUp_shouldSaveAndReturnTrue() {
		// Setup
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("123456");
		request.setEmail("john@example.com");
		request.setFullName("John Doe");

		Role userRole = Role.builder().id(1).roleName("USER").build();
		User savedUser = User.builder().id(1).username("john").email("john@example.com").fullname("John Doe")
				.password("encodedPassword").deleted(false).roles(List.of(userRole)).build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
		when(roleRepository.findByRoleName(SecurityRole.ROLE_USER)).thenReturn(Optional.of(userRole));
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		doNothing().when(producer).sendUserRegistered(any(CreateUserProfileDTO.class));

		// Execute
		Boolean result = userService.signUp(request);

		// Verify
		assertTrue(result);
		verify(userRepository, times(1)).findByUsername("john");
		verify(passwordEncoder, times(1)).encode("123456");
		verify(roleRepository, times(1)).findByRoleName(SecurityRole.ROLE_USER);
		verify(userRepository, times(1)).save(any(User.class));
		verify(producer, times(1)).sendUserRegistered(any(CreateUserProfileDTO.class));
	}

	// === TEST signUp() - username already exists ===
	@Test
	void signUp_shouldThrowExceptionIfUsernameExists() {
		// Setup
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("123456");
		request.setEmail("john@example.com");
		request.setFullName("John Doe");

		User existingUser = User.builder().id(1).username("john").build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(existingUser));

		// Execute & Verify
		assertThrows(UserAlreadyExistException.class, () -> {
			userService.signUp(request);
		});

		// Verify
		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, never()).save(any(User.class));
		verify(producer, never()).sendUserRegistered(any(CreateUserProfileDTO.class));
	}

	// === TEST signUp() - with custom role ===
	@Test
	void signUp_shouldSaveWithCustomRole() {
		// Setup
		SignUpRequest request = new SignUpRequest();
		request.setUsername("admin");
		request.setPassword("123456");
		request.setEmail("admin@example.com");
		request.setFullName("Admin User");
		SignUpRequest.RoleRequest roleRequest = new SignUpRequest.RoleRequest();
		roleRequest.setRoleName("ADMIN");
		request.setRole(roleRequest);

		Role adminRole = Role.builder().id(2).roleName("ADMIN").build();
		User savedUser = User.builder().id(1).username("admin").email("admin@example.com").fullname("Admin User")
				.password("encodedPassword").deleted(false).roles(List.of(adminRole)).build();

		when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
		when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		doNothing().when(producer).sendUserRegistered(any(CreateUserProfileDTO.class));

		// Execute
		Boolean result = userService.signUp(request);

		// Verify
		assertTrue(result);
		verify(roleRepository, times(1)).findByRoleName("ADMIN");
		verify(userRepository, times(1)).save(any(User.class));
	}

	// === TEST signIn() - success ===
	@Test
	void signIn_shouldReturnSignInResponse() {
		// Setup
		SignInRequest request = new SignInRequest();
		request.setUsername("john");
		request.setPassword("123456");

		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("john").password("encodedPassword").email("john@example.com")
				.roles(List.of(userRole)).build();

		CustomUserDetails userDetails = new CustomUserDetails(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		String expectedToken = "test-jwt-token";
		Date expectedExpiration = new Date(System.currentTimeMillis() + 1000 * JWT_DURATION);

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(jwtUtils.generateToken(any(CustomUserDetails.class))).thenReturn(expectedToken);

		// Execute
		SignInResponse result = userService.signIn(request);

		// Verify
		assertNotNull(result);
		assertEquals(expectedToken, result.getToken());
		assertNotNull(result.getExpiredDate());

		verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(jwtUtils, times(1)).generateToken(any(CustomUserDetails.class));
	}

	// === TEST signIn() - authentication failure ===
	@Test
	void signIn_shouldThrowExceptionIfAuthenticationFails() {
		// Setup
		SignInRequest request = new SignInRequest();
		request.setUsername("john");
		request.setPassword("wrongPassword");

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		// Execute & Verify
		assertThrows(BadCredentialsException.class, () -> {
			userService.signIn(request);
		});

		// Verify
		verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(jwtUtils, never()).generateToken(any(CustomUserDetails.class));
	}
}
