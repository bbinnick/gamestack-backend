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
	public ResponseEntity<AuthResponse> createUserHandler(@RequestBody User user) {
		String username = user.getUsername();
		String email = user.getEmail();
		String password = user.getPassword();
		String role = user.getRole();

		User isEmailExisting = userRepository.findByEmail(email);
		if (isEmailExisting != null) {
			AuthResponse authResponse = new AuthResponse();
			authResponse.setMessage("Email Is Already Used With Another Account");
			authResponse.setStatus(false);
			return new ResponseEntity<>(authResponse, HttpStatus.BAD_REQUEST);
		}
		User createdUser = new User();
		createdUser.setUsername(username);
		createdUser.setEmail(email);
		createdUser.setRole(role);
		createdUser.setPassword(passwordEncoder.encode(password));

		User savedUser = userRepository.save(createdUser);
		userRepository.save(savedUser);
		Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String token = JwtProvider.generateToken(authentication);

		AuthResponse authResponse = new AuthResponse();
		authResponse.setJwt(token);
		authResponse.setMessage("Register Success");
		authResponse.setStatus(true);
		return new ResponseEntity<>(authResponse, HttpStatus.OK);

	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> signin(@RequestBody User loginRequest) {
		String username = loginRequest.getEmail();
		String password = loginRequest.getPassword();
		Authentication authentication = authenticate(username, password);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String token = JwtProvider.generateToken(authentication);
		AuthResponse authResponse = new AuthResponse();
		authResponse.setMessage("Login success");
		authResponse.setJwt(token);
		authResponse.setStatus(true);
		
		return new ResponseEntity<>(authResponse, HttpStatus.OK);
	}

	private Authentication authenticate(String username, String password) {

		log.info("Username: {} Password: {}", username, password);
		UserDetails userDetails = customUserDetails.loadUserByUsername(username);
		if (userDetails == null) {
			log.error("Sign in details - null {}", userDetails);
			throw new BadCredentialsException("Invalid username and password");
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

	@GetMapping("/get")
	public User getUser(@RequestBody User user) {
		// "User retrieved successfully"
		return userService.getUserById(user.getId());
	}

	@PutMapping("/delete")
	public void deleteUser(@RequestBody User user) {
		// "User deleted successfully"
		userService.deleteUser(user.getId());
	}

	@PutMapping("/update")
	public User updateUser(@RequestBody User user) {
		// "User updated successfully"
		return userService.updateUser(user);
	}
	
	*/

}
