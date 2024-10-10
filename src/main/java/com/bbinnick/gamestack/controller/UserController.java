package com.bbinnick.gamestack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.model.UserDTO;
import com.bbinnick.gamestack.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@CrossOrigin
@Slf4j
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	// consider creating a separate class for exception handling
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

}
