package com.r2s.auth.service.impl;

import com.r2s.auth.constant.SecurityRole;
import com.r2s.auth.dto.request.SignInRequest;
import com.r2s.auth.dto.request.SignUpRequest;
import com.r2s.auth.dto.response.SignInResponse;
import com.r2s.auth.entity.User;
import com.r2s.auth.repository.RoleRepository;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.security.CustomUserDetails;
import com.r2s.auth.service.UserService;
import com.r2s.auth.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceIMPL implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public Boolean signUp(SignUpRequest request) {
        // check exists userName
        this.userRepository.findByUsername(request.getUsername()).ifPresent((u) -> {
            throw new RuntimeException("User with userName: %s already existed!".formatted(u.getUsername()));
        });
        
        // Create new User entity
        User user = User.builder()
                .username(request.getUsername())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .deleted(false)
                .build();
        
        // Set default role
        user.setRoles(this.roleRepository.findByRoleName(SecurityRole.ROLE_USER));
        this.userRepository.save(user);

        return true;
    }

    @Override
    public SignInResponse signIn(SignInRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        // Get user details
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        // Generate JWT token
        String token = jwtUtils.generateToken(userDetails);
        
        // Calculate expiration date
        java.util.Date expirationDate = new java.util.Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24); // 24 hours
        
        return SignInResponse.builder()
                .token(token)
                .expiredDate(expirationDate)
                .build();
    }
}
