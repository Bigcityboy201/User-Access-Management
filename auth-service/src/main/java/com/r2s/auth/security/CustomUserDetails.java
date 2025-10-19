package com.r2s.auth.security;

import com.r2s.auth.entity.Role;
import com.r2s.auth.entity.User;
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
    private final Set<GrantedAuthority>authorities;//ví dụ:role_ADMIN,....
    private final Set<Role>role;//role ở dứi db bình thường

    public CustomUserDetails(final User user){
        this.username=user.getUsername();
        this.password=user.getPassword();
        this.authorities=user.getRoles().stream().map(role->new SimpleGrantedAuthority(role.getRoleName())).collect(Collectors.toSet());
        this.role=user.getRoles().stream().collect(Collectors.toSet());
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
}
