package com.bbinnick.gamestack.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						// Public endpoints
						.requestMatchers("/users/register", "/users/login").permitAll()
						// Protected endpoints
						.requestMatchers("/games/**").hasRole("USER")
						// Admin-specific rules
						.requestMatchers("/admin/**").hasRole("ADMIN").anyRequest().authenticated())
				.addFilterBefore(new JwtTokenValidator(), BasicAuthenticationFilter.class)
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		return http.build();
	}

	private CorsConfigurationSource corsConfigurationSource() {
		return new CorsConfigurationSource() {
			@Override
			public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
				CorsConfiguration ccfg = new CorsConfiguration();
				ccfg.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
				ccfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
				ccfg.setAllowCredentials(true);
				ccfg.setAllowedHeaders(Collections.singletonList("*"));
				ccfg.setExposedHeaders(Arrays.asList("Authorization"));
				ccfg.setMaxAge(3600L);
				return ccfg;
			}
		};
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
