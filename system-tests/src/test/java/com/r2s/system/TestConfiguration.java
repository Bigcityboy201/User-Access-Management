package com.r2s.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.util.JwtUtils;

/**
 * Test configuration for system tests
 * This allows system tests to use core module components
 */
@SpringBootApplication
@EntityScan(basePackageClasses = { User.class, Role.class })
@EnableJpaRepositories(basePackageClasses = { UserRepository.class, RoleRepository.class })
@ComponentScan(basePackages = {"com.r2s.system", "com.r2s.core"})
public class TestConfiguration {

	@Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
	private String jwtSecret;

	@Value("${jwt.duration:86400}")
	private long jwtDuration;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JwtUtils jwtUtils() {
		JwtUtils jwtUtils = new JwtUtils();
		// Set values using reflection for testing
		try {
			java.lang.reflect.Field secretField = JwtUtils.class.getDeclaredField("jwtSecret");
			secretField.setAccessible(true);
			secretField.set(jwtUtils, jwtSecret);

			java.lang.reflect.Field durationField = JwtUtils.class.getDeclaredField("jwtDuration");
			durationField.setAccessible(true);
			durationField.set(jwtUtils, jwtDuration);
		} catch (Exception e) {
			throw new RuntimeException("Failed to configure JwtUtils", e);
		}
		return jwtUtils;
	}
}

