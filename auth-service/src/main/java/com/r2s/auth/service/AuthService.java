package com.r2s.auth.service;

import com.r2s.auth.dto.response.LoginResponse;
import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
import com.r2s.auth.repository.UserRepository;
import com.r2s.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RoleService roleService;

    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        String token = jwtUtil.generateToken(user.getUsername());
        String roleName = user.getRoles().isEmpty() ? "USER" : user.getRoles().get(0).getName();
        return new LoginResponse(token, user.getUsername(), roleName);
    }

    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Tìm role hoặc tạo role mặc định
        Role role;
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                role = roleService.getRoleByName(request.getRole());
            } catch (RuntimeException e) {
                // Nếu role không tồn tại, tạo role mới
                role = roleService.createRole(request.getRole(), "Auto-created role");
            }
        } else {
            // Tạo hoặc lấy role USER mặc định
            try {
                role = roleService.getRoleByName("USER");
            } catch (RuntimeException e) {
                role = roleService.createRole("USER", "Default user role");
            }
        }
        
        user.getRoles().add(role);
        userRepository.save(user);
        return "User registered successfully";
    }
}
