package com.bbinnick.gamestack.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bbinnick.gamestack.config.JwtProvider;
import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.repository.UserRepository;
import com.bbinnick.gamestack.response.AuthResponse;
import com.bbinnick.gamestack.service.UserServiceImpl;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

	private final JwtProvider jwtProvider;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserServiceImpl customUserDetails;

	@Autowired
	public UserController(JwtProvider jwtProvider, UserRepository userRepository, PasswordEncoder passwordEncoder,
			UserServiceImpl customUserDetails) {
		this.jwtProvider = jwtProvider;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.customUserDetails = customUserDetails;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> createAccount(@RequestBody User user) {
		if (userRepository.findByEmail(user.getEmail()) != null) {
			return new ResponseEntity<>(
					new AuthResponse(null, "Email Is Already Used With Another Account", false, null),
					HttpStatus.BAD_REQUEST);
		}
		if (user.getUsername() == null || user.getUsername().isEmpty()) {
			return new ResponseEntity<>(new AuthResponse(null, "Username Is Required", false, null),
					HttpStatus.BAD_REQUEST);
		}
		if (userRepository.findByUsername(user.getUsername()) != null) {
			return new ResponseEntity<>(
					new AuthResponse(null, "Username Is Already Used With Another Account", false, null),
					HttpStatus.BAD_REQUEST);
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		User savedUser = userRepository.save(user);
		log.info("User registered successfully: {}", savedUser);

		// Set up authorities for the registered user
		List<GrantedAuthority> authorities = List
				.of(new SimpleGrantedAuthority("ROLE_" + savedUser.getRole().toUpperCase()));

		// Create authentication token for the new user
		Authentication authentication = new UsernamePasswordAuthenticationToken(savedUser.getUsername(),
				savedUser.getPassword(), authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Generate JWT token using JwtProvider instance
		String token = jwtProvider.generateToken(authentication);

		// Return response with the token
		AuthResponse authResponse = new AuthResponse(token, "Register Success", true, savedUser.getUsername());
		return new ResponseEntity<>(authResponse, HttpStatus.OK);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> signIn(@RequestBody User loginRequest) {
		// Authenticate user
		Authentication authentication = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Generate JWT token using JwtProvider instance
		String token = jwtProvider.generateToken(authentication);

		// Return response with the token
		AuthResponse authResponse = new AuthResponse(token, "Login Success", true, loginRequest.getUsername());
		return new ResponseEntity<>(authResponse, HttpStatus.OK);
	}

	private Authentication authenticate(String email, String password) {
		// Load user by email
		UserDetails userDetails = customUserDetails.loadUserByEmail(email);
		if (userDetails == null) {
			log.error("Sign in details - null {}", userDetails);
			throw new BadCredentialsException("Invalid email and password");
		}
		// Verify password
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			log.error("Sign in userDetails - password mismatch {}", userDetails);
			throw new BadCredentialsException("Invalid password");
		}
		// Return authentication token for the user
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

	public String generateToken(Authentication authentication) {
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		Map<String, Object> claims = new HashMap<>();
		claims.put("username", userDetails.getUsername());
		claims.put("authorities", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(",")));
		return Jwts.builder().claims(claims).subject(userDetails.getUsername()).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + jwtProvider.getJwtConstant().getExpirationTime()))
				.signWith(jwtProvider.getKey()) // Fetch SecretKey from JwtProvider
				.compact();
	}
}
