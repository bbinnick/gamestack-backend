package com.bbinnick.gamestack.service;

import java.util.List;

import com.bbinnick.gamestack.model.User;

public interface UserService {

	User saveUser(User user);
	
	List<User> getUsers();

	User getUserById(Long id);

	void deleteUser(Long id);

	User getUserByEmail(String email);

	User updateUser(User user);
}