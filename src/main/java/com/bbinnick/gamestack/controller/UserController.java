package com.bbinnick.gamestack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bbinnick.gamestack.dto.UserDTO;
import com.bbinnick.gamestack.dto.UserLoginDTO;
import com.bbinnick.gamestack.dto.UserRegistrationDTO;
import com.bbinnick.gamestack.response.AuthResponse;
import com.bbinnick.gamestack.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> createAccount(@RequestBody UserRegistrationDTO userRegistrationDTO) {
		AuthResponse response = userService.registerUser(userRegistrationDTO);
		if (!response.isSuccess()) {
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> signIn(@RequestBody UserLoginDTO loginDTO) {
		AuthResponse response = userService.loginUser(loginDTO);
		if (!response.isSuccess()) {
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/all")
	public ResponseEntity<List<UserDTO>> getAllUsers() {
		List<UserDTO> users = userService.getAllUsers();
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
		userService.deleteUser(userId);
		return new ResponseEntity<>("User deleted", HttpStatus.OK);
	}
}
