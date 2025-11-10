package com.r2s.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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
	void getAllUsers_shouldReturnListOfUsers() {
		Role userRole = Role.builder().id(1).roleName("USER").build();
		Role adminRole = Role.builder().id(2).roleName("ADMIN").build();

		List<User> mockUsers = List.of(
				User.builder().id(1).username("john").fullname("John Doe").email("john@example.com")
						.roles(List.of(userRole)).build(),
				User.builder().id(2).username("jane").fullname("Jane Smith").email("jane@example.com")
						.roles(List.of(adminRole)).build());

		when(userRepository.findAll()).thenReturn(mockUsers);

		List<User> result = userService.getAllUsers();

		assertEquals(2, result.size());
		assertEquals("john", result.get(0).getUsername());
		assertEquals("jane", result.get(1).getUsername());

		verify(userRepository, times(1)).findAll();
	}

	// ===== getUserByUsername =====
	@Test
	void getUserByUsername_shouldReturnUserResponse() {
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").fullname("John Doe").email("john@example.com")
				.roles(List.of(userRole)).build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));

		UserResponse result = userService.getUserByUsername("john");

		assertEquals("john", result.getUsername());
		assertEquals("John Doe", result.getFullname());
		assertEquals("john@example.com", result.getEmail());

		verify(userRepository, times(1)).findByUsername("john");
	}

	@Test
	void getUserByUsername_shouldThrowIfNotFound() {
		when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("missing"));

		verify(userRepository, times(1)).findByUsername("missing");
	}

	// ===== updateUser =====
	@Test
	void updateUser_shouldUpdateAndReturnUserResponse() {
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").fullname("Old Name").email("old@example.com")
				.roles(List.of(userRole)).build();

		UpdateUserRequest update = new UpdateUserRequest();
		update.setFullName("New Name");
		update.setEmail("new@example.com");

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
		when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
		when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArgument(0));

		UserResponse result = userService.updateUser("john", update);

		assertEquals("New Name", result.getFullname());
		assertEquals("new@example.com", result.getEmail());

		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, times(1)).findByEmail("new@example.com");
		verify(userRepository, times(1)).save(mockUser);
	}

	@Test
	void updateUser_shouldThrowIfEmailExistsForAnotherUser() {
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").email("old@example.com").fullname("John Doe")
				.roles(List.of(userRole)).build();
		User existingUser = User.builder().id(2).username("jane").email("new@example.com").roles(List.of(userRole))
				.build();

		UpdateUserRequest update = new UpdateUserRequest();
		update.setEmail("new@example.com");

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
		when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(existingUser));

		assertThrows(UserNotFoundException.class, () -> userService.updateUser("john", update));

		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, times(1)).findByEmail("new@example.com");
		verify(userRepository, never()).save(Mockito.any());
	}

	// ===== deleteUser =====
	@Test
	void deleteUser_shouldDeleteIfExists() {
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").email("john@example.com").roles(List.of(userRole))
				.build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));

		userService.deleteUser("john");

		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, times(1)).delete(mockUser);
	}

	@Test
	void deleteUser_shouldThrowIfUserNotFound() {
		when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.deleteUser("missing"));

		verify(userRepository, times(1)).findByUsername("missing");
		verify(userRepository, never()).delete(Mockito.any());
	}

	@Test
	void deleteUser_shouldThrowRuntimeExceptionIfDeleteFails() {
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User mockUser = User.builder().id(1).username("john").email("john@example.com").roles(List.of(userRole))
				.build();

		when(userRepository.findByUsername("john")).thenReturn(Optional.of(mockUser));
		doThrow(new RuntimeException("DB error")).when(userRepository).delete(mockUser);

		assertThrows(RuntimeException.class, () -> userService.deleteUser("john"));

		verify(userRepository, times(1)).findByUsername("john");
		verify(userRepository, times(1)).delete(mockUser);
	}
}
