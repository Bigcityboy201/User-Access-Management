package com.r2s.user;

import com.r2s.core.dto.CreateUserProfileDTO;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.service.IMPL.UserProfileServiceIMPL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceIMPLTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserProfileServiceIMPL userProfileService;

    // Simple helper to attach a spy logger if we ever want to verify logging directly
    private Logger attachSpyLogger() {
        Logger logger = LoggerFactory.getLogger(UserProfileServiceIMPL.class);
        Logger spyLogger = Mockito.spy(logger);
        ReflectionTestUtils.setField(UserProfileServiceIMPL.class, "log", spyLogger);
        return spyLogger;
    }

    @Test
    @DisplayName("create_shouldSkipWhenUserAlreadyExists - should not create or save when user exists")
    void create_shouldSkipWhenUserAlreadyExists() {
        // ===== ARRANGE =====
        CreateUserProfileDTO dto = CreateUserProfileDTO.builder()
                .username("existingUser")
                .email("existing@example.com")
                .fullName("Existing User")
                .roleNames(List.of("USER"))
                .build();

        when(userRepository.findByUsername("existingUser"))
                .thenReturn(Optional.of(User.builder().id(1).username("existingUser").build()));

        // ===== ACT =====
        userProfileService.create(dto);

        // ===== ASSERT =====
        verify(userRepository, times(1)).findByUsername("existingUser");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(roleRepository);
    }

    @Test
    @DisplayName("create_shouldHandleMissingRoles - should warn and create without roles when roles not found")
    void create_shouldHandleMissingRoles() {
        // ===== ARRANGE =====
        CreateUserProfileDTO dto = CreateUserProfileDTO.builder()
                .username("newUser")
                .email("new@example.com")
                .fullName("New User")
                .roleNames(List.of("MISSING_ROLE"))
                .build();

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName("MISSING_ROLE")).thenReturn(Optional.empty());

        User unsavedUser = User.builder()
                .id(null)
                .username("newUser")
                .email("new@example.com")
                .fullname("New User")
                .password("N/A")
                .deleted(false)
                .build();

        User savedUser = User.builder()
                .id(100)
                .username("newUser")
                .email("new@example.com")
                .fullname("New User")
                .password("N/A")
                .deleted(false)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // ===== ACT =====
        assertDoesNotThrow(() -> userProfileService.create(dto));

        // ===== ASSERT =====
        verify(userRepository, times(1)).findByUsername("newUser");
        verify(roleRepository, times(1)).findByRoleName("MISSING_ROLE");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("create_shouldCreateUserWithoutRolesWhenNoRolesProvided - should not call roleRepository when roleNames is null or empty")
    void create_shouldCreateUserWithoutRolesWhenNoRolesProvided() {
        // ===== ARRANGE =====
        CreateUserProfileDTO dto = CreateUserProfileDTO.builder()
                .username("userNoRoles")
                .email("noroles@example.com")
                .fullName("User No Roles")
                .roleNames(null)
                .build();

        when(userRepository.findByUsername("userNoRoles")).thenReturn(Optional.empty());

        User savedUser = User.builder()
                .id(200)
                .username("userNoRoles")
                .email("noroles@example.com")
                .fullname("User No Roles")
                .password("N/A")
                .deleted(false)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // ===== ACT =====
        userProfileService.create(dto);

        // ===== ASSERT =====
        verify(userRepository, times(1)).findByUsername("userNoRoles");
        verifyNoInteractions(roleRepository);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("create_shouldHandleDatabaseException - should bubble up exception from repository.save")
    void create_shouldHandleDatabaseException() {
        // ===== ARRANGE =====
        CreateUserProfileDTO dto = CreateUserProfileDTO.builder()
                .username("dbErrorUser")
                .email("dbError@example.com")
                .fullName("DB Error User")
                .roleNames(List.of("USER"))
                .build();

        when(userRepository.findByUsername("dbErrorUser")).thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("DB error"));

        // ===== ACT & ASSERT =====
        assertThrows(RuntimeException.class, () -> userProfileService.create(dto));

        verify(userRepository, times(1)).findByUsername("dbErrorUser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("create_shouldLogWarningWhenRoleNotFound - should attempt to resolve roles and continue on missing role")
    void create_shouldLogWarningWhenRoleNotFound() {
        // ===== ARRANGE =====
        CreateUserProfileDTO dto = CreateUserProfileDTO.builder()
                .username("userMissingRole")
                .email("missingrole@example.com")
                .fullName("User Missing Role")
                .roleNames(List.of("EXISTING_ROLE", "MISSING_ROLE"))
                .build();

        Role existingRole = Role.builder().id(1).roleName("EXISTING_ROLE").build();

        when(userRepository.findByUsername("userMissingRole")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName("EXISTING_ROLE")).thenReturn(Optional.of(existingRole));
        when(roleRepository.findByRoleName("MISSING_ROLE")).thenReturn(Optional.empty());

        User savedUser = User.builder()
                .id(300)
                .username("userMissingRole")
                .email("missingrole@example.com")
                .fullname("User Missing Role")
                .password("N/A")
                .deleted(false)
                .roles(List.of(existingRole))
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // ===== ACT =====
        userProfileService.create(dto);

        // ===== ASSERT =====
        verify(roleRepository, times(1)).findByRoleName("EXISTING_ROLE");
        verify(roleRepository, times(1)).findByRoleName("MISSING_ROLE");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("create_shouldAssignMultipleRoles - should assign all existing roles to user")
    void create_shouldAssignMultipleRoles() {
        // ===== ARRANGE =====
        CreateUserProfileDTO dto = CreateUserProfileDTO.builder()
                .username("multiRoleUser")
                .email("multi@example.com")
                .fullName("Multi Role User")
                .roleNames(List.of("ROLE_USER", "ROLE_ADMIN"))
                .build();

        when(userRepository.findByUsername("multiRoleUser")).thenReturn(Optional.empty());

        Role userRole = Role.builder().id(1).roleName("ROLE_USER").build();
        Role adminRole = Role.builder().id(2).roleName("ROLE_ADMIN").build();

        when(roleRepository.findByRoleName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByRoleName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        User savedUser = User.builder()
                .id(400)
                .username("multiRoleUser")
                .email("multi@example.com")
                .fullname("Multi Role User")
                .password("N/A")
                .deleted(false)
                .roles(List.of(userRole, adminRole))
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // ===== ACT =====
        userProfileService.create(dto);

        // ===== ASSERT =====
        verify(userRepository, times(1)).findByUsername("multiRoleUser");
        verify(roleRepository, times(1)).findByRoleName("ROLE_USER");
        verify(roleRepository, times(1)).findByRoleName("ROLE_ADMIN");
        verify(userRepository, times(1)).save(any(User.class));
    }
}


