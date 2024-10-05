package com.bbinnick.gamestack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.repository.UserRepository;

public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	@Override
	public User saveUser(User user) {
		return userRepo.save(user);
	}

	@Override
	public List<User> getUsers() {
		return userRepo.findAll();
	}

	@Override
	public User getUserById(Long id) {
		return userRepo.findById(id).get();
	}

	@Override
	public void deleteUser(Long id) {
		userRepo.deleteById(id);
	}

	@Override
	public User getUserByEmail(String email) {
		return userRepo.findByEmail(email);
	}

	@Override
	public User updateUser(User user) {
		return userRepo.save(user);
	}

}
