package com.r2s.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.exception.UserNotFoundException;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.UserResponse;
import com.r2s.user.dto.UserResponse.UpdateUserRequest;
import com.r2s.user.service.IMPL.UserServiceIMPL;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserServiceIMPL userService;

	// ===== getAllUsers =====
	@Test
	@DisplayName("getAllUsers - Should return list of users")
	void getAllUsers_shouldReturnListOfUsers_whenUsersExist() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		Role adminRole = Role.builder().id(2).roleName("ADMIN").build();

		List<User> mockUsers = List.of(
				User.builder().id(1).username("john").fullname("John Doe").email("john@example.com")
						.roles(List.of(userRole)).build(),
				User.builder().id(2).username("jane").fullname("Jane Smith").email("jane@example.com")
						.roles(List.of(adminRole)).build());

		when(userRepository.findAll()).thenReturn(mockUsers);

		// ===== ACT =====
		List<User> result = userService.getAllUsers();

		// ===== ASSERT =====
		assertEquals(2, result.size());
		assertEquals("john", result.get(0).getUsername());
		assertEquals("jane", result.get(1).getUsername());
		verify(userRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("getAllUsers - Should return empty list when no users")
	void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
		// ===== ARRANGE =====
		when(userRepository.findAll()).thenReturn(List.of());

		// ===== ACT =====
		List<User> result = userService.getAllUsers();

		// ===== ASSERT =====
		assertEquals(0, result.size());
		verify(userRepository, times(1)).findAll();
	}

	// ===== getUserByUsername =====
	@Test
	@DisplayName("getUserByUsername - Should return user response")
	void getUserByUsername_shouldReturnUserResponse_whenUserExists() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").fullname("John Doe").email("john@example.com")
				.roles(List.of(userRole)).build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));

		// ===== ACT =====
		UserResponse result = userService.getUserByUsername("john");

		// ===== ASSERT =====
		assertEquals("john", result.getUsername());
		assertEquals("John Doe", result.getFullname());
		assertEquals("john@example.com", result.getEmail());
		verify(userRepository, times(1)).findByUsername("john");
	}

	@Test
	@DisplayName("getUserByUsername - Should throw exception when user not found")
	void getUserByUsername_shouldThrowIfNotFound_whenUserMissing() {
		// ===== ARRANGE =====
		when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

		// ===== ACT & ASSERT =====
		assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("missing"));
		verify(userRepository, times(1)).findByUsername("missing");
	}

	// ===== updateUser =====
	@Test
	@DisplayName("updateUser - Should update user successfully")
	void updateUser_shouldUpdateAndReturnUserResponse_whenValidData() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").fullname("Old Name").email("old@example.com")
				.roles(List.of(userRole)).build();

		UpdateUserRequest update = new UpdateUserRequest();
		update.setFullName("New Name");
		update.setEmail("new@example.com");

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
		when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
		when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArgument(0));

		// ===== ACT =====
		UserResponse result = userService.updateUser("john", update);

		// ===== ASSERT =====
		assertEquals("New Name", result.getFullname());
		assertEquals("new@example.com", result.getEmail());
		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, times(1)).findByEmail("new@example.com");
		verify(userRepository, times(1)).save(mockUser);
	}

	@Test
	@DisplayName("updateUser - Should not update if email is same as current")
	void updateUser_shouldNotUpdate_whenEmailSameAsCurrent() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").fullname("John Doe").email("john@example.com")
				.roles(List.of(userRole)).build();

		UpdateUserRequest update = new UpdateUserRequest();
		update.setEmail("john@example.com"); // same as current
		update.setFullName("John Doe Updated");

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));

		// ===== ACT =====
		UserResponse result = userService.updateUser("john", update);

		// ===== ASSERT =====
		assertEquals("John Doe Updated", result.getFullname());
		assertEquals("john@example.com", result.getEmail());
		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, never()).findByEmail(Mockito.any());
		verify(userRepository, times(1)).save(mockUser);
	}

	@Test
	@DisplayName("updateUser - Should handle null fields gracefully")
	void updateUser_shouldHandleNullFields_whenFieldsAreNull() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").fullname("John Doe").email("john@example.com")
				.roles(List.of(userRole)).build();

		UpdateUserRequest update = new UpdateUserRequest(); // all fields null

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
		when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArgument(0));

		// ===== ACT =====
		UserResponse result = userService.updateUser("john", update);

		// ===== ASSERT =====
		assertEquals("John Doe", result.getFullname());
		assertEquals("john@example.com", result.getEmail());
		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, never()).findByEmail(Mockito.any());
		verify(userRepository, times(1)).save(mockUser);
	}

	@Test
	@DisplayName("updateUser - Should throw exception if email exists for another user")
	void updateUser_shouldThrowIfEmailExistsForAnotherUser_whenEmailTaken() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").email("old@example.com").fullname("John Doe")
				.roles(List.of(userRole)).build();
		User existingUser = User.builder().id(2).username("jane").email("new@example.com").roles(List.of(userRole))
				.build();

		UpdateUserRequest update = new UpdateUserRequest();
		update.setEmail("new@example.com");

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
		when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(existingUser));

		// ===== ACT & ASSERT =====
		assertThrows(UserNotFoundException.class, () -> userService.updateUser("john", update));
		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, times(1)).findByEmail("new@example.com");
		verify(userRepository, never()).save(Mockito.any());
	}

	// ===== deleteUser =====
	@Test
	@DisplayName("deleteUser - Should delete user if exists")
	void deleteUser_shouldDeleteIfExists_whenUserExists() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").email("john@example.com").roles(List.of(userRole))
				.build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));

		// ===== ACT =====
		userService.deleteUser("john");

		// ===== ASSERT =====
		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, times(1)).delete(mockUser);
	}

	@Test
	@DisplayName("deleteUser - Should throw exception if user not found")
	void deleteUser_shouldThrowIfUserNotFound_whenMissing() {
		// ===== ARRANGE =====
		when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

		// ===== ACT & ASSERT =====
		assertThrows(UserNotFoundException.class, () -> userService.deleteUser("missing"));
		verify(userRepository, times(1)).findByUsername("missing");
		verify(userRepository, never()).delete(Mockito.any());
	}

	@Test
	@DisplayName("deleteUser - Should throw RuntimeException if delete fails")
	void deleteUser_shouldThrowRuntimeExceptionIfDeleteFails_whenDBError() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").email("john@example.com").roles(List.of(userRole))
				.build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
		Mockito.doThrow(new RuntimeException("DB error")).when(userRepository).delete(mockUser);

		// ===== ACT & ASSERT =====
		assertThrows(RuntimeException.class, () -> userService.deleteUser("john"));
		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, times(1)).delete(mockUser);
	}
}
