package com.r2s.core.security;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Setter
public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;
    private final String username;
    private final String password;
    private final Set<GrantedAuthority>authorities;//vi du:role_ADMIN,....
    private final Set<Role>role;//role o duoi db binh thuong

    public CustomUserDetails(final User user){
        this.username=user.getUsername();
        this.password=user.getPassword();
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            this.authorities=user.getRoles().stream()
                    .map(role->new SimpleGrantedAuthority("ROLE_" + role.getRoleName().toUpperCase()))
                    .collect(Collectors.toSet());
            this.role=user.getRoles().stream().collect(Collectors.toSet());
        } else {
            this.authorities = Set.of();
            this.role = Set.of();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public Set<Role> getRole() {
        return this.role;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
