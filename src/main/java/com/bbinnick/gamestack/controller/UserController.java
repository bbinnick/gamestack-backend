package com.bbinnick.gamestack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bbinnick.gamestack.auth.SecurityUser;
import com.bbinnick.gamestack.config.JwtProvider;
import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.repository.UserRepository;
import com.bbinnick.gamestack.response.AuthResponse;
import com.bbinnick.gamestack.service.UserServiceImpl;

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
		SecurityUser securityUser = new SecurityUser(savedUser);
		Authentication authentication = new UsernamePasswordAuthenticationToken(securityUser,
				securityUser.getPassword(), securityUser.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String token = jwtProvider.generateToken(authentication);
		AuthResponse authResponse = new AuthResponse(token, "Register Success", true, savedUser.getUsername());
		return new ResponseEntity<>(authResponse, HttpStatus.OK);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> signIn(@RequestBody User loginRequest) {
		Authentication authentication = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String token = jwtProvider.generateToken(authentication);
		AuthResponse authResponse = new AuthResponse(token, "Login Success", true, loginRequest.getUsername());
		return new ResponseEntity<>(authResponse, HttpStatus.OK);
	}

	private Authentication authenticate(String email, String password) {
		UserDetails userDetails = customUserDetails.loadUserByEmail(email);
		if (userDetails == null) {
			log.error("Sign in failed - user not found: {}", email);
			throw new BadCredentialsException("Invalid email and password");
		}

		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			log.error("Sign in failed - password mismatch for user: {}", userDetails.getUsername());
			throw new BadCredentialsException("Invalid password");
		}
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}

}
