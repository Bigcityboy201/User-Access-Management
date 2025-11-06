package com.r2s.core.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.r2s.core.constant.SecurityConstants;
import com.r2s.core.util.JwtUtils;

import io.jsonwebtoken.lang.Strings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;
	private final UserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		// Get JWT token from HTTP request
		String token = this.getTokenFromRequest(request);

		// Validate Token
		// hasText(token):kiểm tra token có tồn tại hay không
        if (StringUtils.hasText(token) && this.jwtUtils.validateToken(token)) {
            String userName = this.jwtUtils.extractUsername(token);

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userName);
            
            // Debug: Log authorities
            System.out.println("=== DEBUG JWT Authentication ===");
            System.out.println("Username: " + userName);
            System.out.println("Authorities: " + userDetails.getAuthorities());
            System.out.println("=================================");

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } else {
            System.out.println("=== DEBUG JWT Authentication FAILED ===");
            System.out.println("Token exists: " + StringUtils.hasText(token));
            if (StringUtils.hasText(token)) {
                System.out.println("Token valid: " + this.jwtUtils.validateToken(token));
            }
            System.out.println("========================================");
        }

		filterChain.doFilter(request, response);
	}

	private String getTokenFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader(SecurityConstants.HEADER_STRING);

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			return bearerToken.substring(SecurityConstants.TOKEN_PREFIX.length());
		}

		return Strings.EMPTY;
	}
}
