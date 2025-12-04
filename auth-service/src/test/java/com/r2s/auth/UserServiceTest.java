package com.r2s.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
	@DisplayName("signUp() should save user successfully when username does not exist")
	void signUp_shouldReturnTrue_whenUserDoesNotExist() {
		// Arrange
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

		// Act
		Boolean result = userService.signUp(request);

		// Assert
		assertTrue(result);
		verify(userRepository, times(1)).findByUsername("john");
		verify(passwordEncoder, times(1)).encode("123456");
		verify(roleRepository, times(1)).findByRoleName(SecurityRole.ROLE_USER);
		verify(userRepository, times(1)).save(any(User.class));
		verify(producer, times(1)).sendUserRegistered(any(CreateUserProfileDTO.class));
	}

	// === TEST signUp() - username already exists ===
	@Test
	@DisplayName("signUp() should throw exception when username already exists")
	void signUp_shouldThrowException_whenUsernameExists() {

		// Arrange
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("123456");
		request.setEmail("john@example.com");
		request.setFullName("John Doe");

		User existingUser = User.builder().id(1).username("john").build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(existingUser));

		// Act
		assertThrows(UserAlreadyExistException.class, () -> {
			userService.signUp(request);
		});

		// Assert
		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, never()).save(any(User.class));
		verify(producer, never()).sendUserRegistered(any(CreateUserProfileDTO.class));
	}

	// === TEST signUp() - with custom role ===
	@Test
	@DisplayName("signUp() should save user with custom role when role exists")
	void signUp_shouldSaveUserWithCustomRole_whenRoleExists() {
		// Arrange
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

		// Act
		Boolean result = userService.signUp(request);

		// Assert
		assertTrue(result);
		verify(roleRepository, times(1)).findByRoleName("ADMIN");
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	@DisplayName("signUp() should throw exception when role is not found")
	void signUp_shouldThrowExceptionWhenRoleNotFound() {
		// Arrange
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("pass");

		SignUpRequest.RoleRequest roleReq = new SignUpRequest.RoleRequest();
		roleReq.setRoleName("MANAGER");
		request.setRole(roleReq);

		when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
		when(roleRepository.findByRoleName("MANAGER")).thenReturn(Optional.empty());

		// Act + Assert
		RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.signUp(request));
		assertEquals("Role not found: MANAGER", ex.getMessage());

		// Verify interactions
		verify(userRepository, times(1)).findByUsername("john");
		verify(roleRepository, times(1)).findByRoleName("MANAGER");
		verify(userRepository, never()).save(any(User.class));
		verify(producer, never()).sendUserRegistered(any(CreateUserProfileDTO.class));
	}

	@Test
	@DisplayName("signUp() should fail gracefully when Kafka producer throws exception")
	void signUp_shouldHandleKafkaProducerFailure() {
		// Arrange
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("123456");
		request.setEmail("john@example.com");
		request.setFullName("John Doe");

		Role role = Role.builder().id(1).roleName(SecurityRole.ROLE_USER).build();
		User savedUser = User.builder().id(1).username("john").email("john@example.com").fullname("John Doe")
				.password("encodedPassword").deleted(false).roles(List.of(role)).build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
		when(roleRepository.findByRoleName(SecurityRole.ROLE_USER)).thenReturn(Optional.of(role));
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		doThrow(new RuntimeException("Kafka send failed")).when(producer)
				.sendUserRegistered(any(CreateUserProfileDTO.class));

		// Act + Assert
		RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.signUp(request));
		assertEquals("Kafka send failed", ex.getMessage());

		// Verify method calls
		verify(userRepository, times(1)).findByUsername("john");
		verify(passwordEncoder, times(1)).encode("123456");
		verify(roleRepository, times(1)).findByRoleName(SecurityRole.ROLE_USER);
		verify(userRepository, times(1)).save(any(User.class));
		verify(producer, times(1)).sendUserRegistered(any(CreateUserProfileDTO.class));
	}

	@Test
	@DisplayName("signUp() should handle database exception when saving user")
	void signUp_shouldHandleDatabaseException() {
		// Arrange
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("123456");
		request.setEmail("john@example.com");
		request.setFullName("John Doe");

		Role role = Role.builder().id(1).roleName(SecurityRole.ROLE_USER).build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
		when(roleRepository.findByRoleName(SecurityRole.ROLE_USER)).thenReturn(Optional.of(role));
		when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

		// Act + Assert
		RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.signUp(request));
		assertEquals("Database error", ex.getMessage());

		// Verify interactions
		verify(userRepository, times(1)).findByUsername("john");
		verify(passwordEncoder, times(1)).encode("123456");
		verify(roleRepository, times(1)).findByRoleName(SecurityRole.ROLE_USER);
		verify(userRepository, times(1)).save(any(User.class));
		verify(producer, never()).sendUserRegistered(any(CreateUserProfileDTO.class));
	}

	@Test
	@DisplayName("signUp() should throw exception when email already exists (database constraint)")
	void signUp_shouldThrowExceptionWhenEmailExists() {
		// Arrange
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("123456");
		request.setEmail("duplicate@example.com");
		request.setFullName("John Doe");

		Role role = Role.builder().id(1).roleName(SecurityRole.ROLE_USER).build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
		when(roleRepository.findByRoleName(SecurityRole.ROLE_USER)).thenReturn(Optional.of(role));
		when(userRepository.save(any(User.class)))
				.thenThrow(new RuntimeException("Email already exists: duplicate@example.com"));

		// Act + Assert
		RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.signUp(request));
		assertEquals("Email already exists: duplicate@example.com", ex.getMessage());

		// Verify interactions
		verify(userRepository, times(1)).findByUsername("john");
		verify(passwordEncoder, times(1)).encode("123456");
		verify(roleRepository, times(1)).findByRoleName(SecurityRole.ROLE_USER);
		verify(userRepository, times(1)).save(any(User.class));
		verify(producer, never()).sendUserRegistered(any(CreateUserProfileDTO.class));
	}

	// === TEST signIn() - success ===
	@Test
	@DisplayName("signIn() should return token when authentication is successful")
	void signIn_shouldReturnToken_whenCredentialsValid() {
		// Arrange
		SignInRequest request = new SignInRequest();
		request.setUsername("john");
		request.setPassword("123");

		User user = new User();
		user.setUsername("john");
		CustomUserDetails userDetails = new CustomUserDetails(user);

		Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
		when(authenticationManager.authenticate(any())).thenReturn(auth);
		when(jwtUtils.generateToken(any())).thenReturn("jwt-token");

		// Act
		SignInResponse response = userService.signIn(request);

		// Assert
		assertNotNull(response);
		assertEquals("jwt-token", response.getToken());
		verify(authenticationManager, times(1)).authenticate(any());
		verify(jwtUtils, times(1)).generateToken(any());
	}

	// === TEST signIn() - authentication failure ===
	@Test
	@DisplayName("signIn() should throw BadCredentialsException when authentication fails")
	void signIn_shouldThrowException_whenAuthenticationFails() {
		// Arrange
		SignInRequest req = new SignInRequest();
		req.setUsername("john");
		req.setPassword("wrong");

		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

		// Act + Assert
		assertThrows(BadCredentialsException.class, () -> userService.signIn(req));
		verify(authenticationManager, times(1)).authenticate(any());
		verify(jwtUtils, never()).generateToken(any());
	}

	@Test
	@DisplayName("signIn() should throw DisabledException when user is deleted/disabled")
	void signIn_shouldThrowExceptionWhenUserDeleted() {
		// Arrange
		SignInRequest req = new SignInRequest();
		req.setUsername("deletedUser");
		req.setPassword("password");

		when(authenticationManager.authenticate(any()))
				.thenThrow(new DisabledException("User is disabled or deleted"));

		// Act + Assert
		assertThrows(DisabledException.class, () -> userService.signIn(req));
		verify(authenticationManager, times(1)).authenticate(any());
		verify(jwtUtils, never()).generateToken(any());
	}

	@Test
	@DisplayName("signIn() should throw UsernameNotFoundException when user is not found")
void signIn_shouldThrowExceptionWhenUserNotFound() {
		// Arrange
		SignInRequest req = new SignInRequest();
		req.setUsername("unknown");
		req.setPassword("password");

		when(authenticationManager.authenticate(any()))
				.thenThrow(new UsernameNotFoundException("User not found"));

		// Act + Assert
		assertThrows(UsernameNotFoundException.class, () -> userService.signIn(req));
		verify(authenticationManager, times(1)).authenticate(any());
		verify(jwtUtils, never()).generateToken(any());
	}

	@Test
	@DisplayName("signIn() should handle token generation failure (e.g., expired token scenario)")
	void signIn_shouldHandleExpiredToken() {
		// Arrange
		SignInRequest request = new SignInRequest();
		request.setUsername("john");
		request.setPassword("123");

		User user = new User();
		user.setUsername("john");
		CustomUserDetails userDetails = new CustomUserDetails(user);

		Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);
		when(authenticationManager.authenticate(any())).thenReturn(auth);
		when(jwtUtils.generateToken(any())).thenThrow(new RuntimeException("Token generation failed"));

		// Act + Assert
		RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.signIn(request));
		assertEquals("Token generation failed", ex.getMessage());
		verify(authenticationManager, times(1)).authenticate(any());
		verify(jwtUtils, times(1)).generateToken(any());
	}
}
