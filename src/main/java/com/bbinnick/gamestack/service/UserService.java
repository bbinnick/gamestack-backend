package com.bbinnick.gamestack.service;

import java.util.List;

import com.bbinnick.gamestack.model.User;

public interface UserService {

	User saveUser(User user);

	List<User> getAllUsers();

	User getUserById(Long id);

	void deleteUser(Long id);

	User getUserByEmail(String email);

	User getUserByUsername(String username);

	// maybe not needed
	User updateUser(User user);
}
