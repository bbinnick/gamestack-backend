package com.bbinnick.gamestack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.service.UserService;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/register")
	public User registerUser(@RequestBody User user) {
		// "New user registered successfully"
		return userService.saveUser(user);
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

	@PostMapping("/login")
	public User loginUser(@RequestBody User user) {
		// "User logged in successfully"
		return userService.getUserByEmail(user.getEmail());
	}

	@PutMapping("/update")
	public User updateUser(@RequestBody User user) {
		// "User updated successfully"
		return userService.updateUser(user);
	}

}
