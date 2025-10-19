package com.r2s.auth.security;

import com.r2s.auth.entity.User;
import com.r2s.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user=this.userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("Can not found UserName: "+username));
        return  new CustomUserDetails(user);
    }
}
