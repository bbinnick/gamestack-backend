package com.bbinnick.gamestack.controller;

import java.util.ArrayList;
import java.util.List;

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

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserServiceImpl customUserDetails;	
	
	@PostMapping("/register")
	public ResponseEntity<AuthResponse> createAccount(@RequestBody User user) {
	    User isEmailExisting = userRepository.findByEmail(user.getEmail());
	    User isUsernameExisting = userRepository.findByUsername(user.getUsername()); 
	    if (isEmailExisting != null) {
	        return new ResponseEntity<>(new AuthResponse(null, "Email Is Already Used With Another Account", false, null), HttpStatus.BAD_REQUEST);
	    }
		if ( user.getUsername() == null || user.getUsername().isEmpty() ) {
			return new ResponseEntity<>(new AuthResponse(null, "Username Is Required", false, null), HttpStatus.BAD_REQUEST);
		}
	    if (isUsernameExisting != null) {
	        return new ResponseEntity<>(new AuthResponse(null, "Username Is Already Used With Another Account", false, null), HttpStatus.BAD_REQUEST);
	    }
	    user.setPassword(passwordEncoder.encode(user.getPassword()));
	    User savedUser = userRepository.save(user);
	    log.info("User registered successfully: {}", savedUser);
	    
	    List<GrantedAuthority> authorities = new ArrayList<>();
	    authorities.add(new SimpleGrantedAuthority("ROLE_" + savedUser.getRole().toUpperCase()));

	    Authentication authentication = new UsernamePasswordAuthenticationToken(savedUser.getUsername(), savedUser.getPassword(), authorities);
	    SecurityContextHolder.getContext().setAuthentication(authentication);
	    String token = JwtProvider.generateToken(authentication);

	    AuthResponse authResponse = new AuthResponse(token, "Register Success", true, user.getUsername());
	    return new ResponseEntity<>(authResponse, HttpStatus.OK);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> signIn(@RequestBody User loginRequest) {
	    Authentication authentication = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
	    SecurityContextHolder.getContext().setAuthentication(authentication);
	    String token = JwtProvider.generateToken(authentication);

	    AuthResponse authResponse = new AuthResponse(token, "Login success", true, loginRequest.getUsername());
	    return new ResponseEntity<>(authResponse, HttpStatus.OK);
	}
	
	private Authentication authenticate(String email, String password) {
	    UserDetails userDetails = customUserDetails.loadUserByEmail(email);
	    if (userDetails == null) {
	        log.error("Sign in details - null {}", userDetails);
	        throw new BadCredentialsException("Invalid email and password");
	    }
	    if (!passwordEncoder.matches(password, userDetails.getPassword())) {
	        log.error("Sign in userDetails - password mismatch {}", userDetails);
	        throw new BadCredentialsException("Invalid password");
	    }
	    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}
	
	// consider creating a separate class for exception handling
	/*
	@PostMapping("/register")
	public User registerUser(@RequestBody User user) {
		try {
			if (user.getUsername() == null || user.getUsername().isEmpty()) {
				throw new IllegalArgumentException("Username is required");
			}
			if (userService.getUserByUsername(user.getUsername()) != null) {
				throw new IllegalArgumentException("Username is already in use");
			}
			if (user.getEmail() == null || user.getEmail().isEmpty()) {
				throw new IllegalArgumentException("Email is required");
			}
			if (userService.getUserByEmail(user.getEmail()) != null) {
				throw new IllegalArgumentException("Email is already in use");
			}
			if (user.getPassword() == null || user.getPassword().isEmpty()) {
				throw new IllegalArgumentException("Password is required");
			}
			return userService.saveUser(user);
		} catch (IllegalArgumentException e) {
			log.error("Error registering user: {}", user, e);
			throw e;
		}
	}	
	
	@PostMapping("/login")
	public UserDTO loginUser(@RequestBody User user) {
		User existingUser = userService.getUserByEmail(user.getEmail());
		if (existingUser == null || !passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
			throw new IllegalArgumentException("Invalid email or password");
		}
		UserDTO userDTO = new UserDTO();
		userDTO.setId(existingUser.getId());
		userDTO.setUsername(existingUser.getUsername());
		userDTO.setEmail(existingUser.getEmail());
		return userDTO;
	}	
	
	@GetMapping("/getAll")
	public List<User> getAllUsers() {
		// "All users retrieved successfully"
		return userService.getAllUsers();
	}

	@PutMapping("/delete")
	public void deleteUser(@RequestBody User user) {
		// "User deleted successfully"
		userService.deleteUser(user.getId());
	}
	*/

}
