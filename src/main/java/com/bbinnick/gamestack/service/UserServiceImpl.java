package com.bbinnick.gamestack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.bbinnick.gamestack.auth.SecurityUser;
import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

	private UserRepository userRepo;
	
	@Autowired
	public UserServiceImpl(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepo.findByUsername(username);
		if (user == null) {
			log.error("User not found with this username: {}", username);
			throw new UsernameNotFoundException("User not found with this username" + username);
		}
		log.info("Loaded user: {}, Email: {}, Role: {}", user.getUsername(), user.getEmail(), user.getRole());
		return new SecurityUser(user);
	}

	public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
		User user = userRepo.findByEmail(email);
		if (user == null) {
			log.error("User not found with this email: {}", email);
			throw new UsernameNotFoundException("User not found with this email" + email);
		}
		log.info("Loaded user: {}", user.getEmail());
		return new SecurityUser(user);
	}
}
