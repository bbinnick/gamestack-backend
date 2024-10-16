package com.bbinnick.gamestack.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.bbinnick.gamestack.config.JwtProvider;
import com.bbinnick.gamestack.controller.dto.UserLoginDTO;
import com.bbinnick.gamestack.controller.dto.UserRegistrationDTO;
import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.repository.UserRepository;
import com.bbinnick.gamestack.response.AuthResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final UserServiceImpl userServiceImpl;

	@Autowired
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider,
			UserServiceImpl userServiceImpl) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtProvider = jwtProvider;
		this.userServiceImpl = userServiceImpl;
	}

	public AuthResponse registerUser(UserRegistrationDTO userDTO) {
		if (userRepository.findByEmail(userDTO.getEmail()) != null)
			return new AuthResponse(null, "Email Is Already Used", false, null);
		if (userRepository.findByUsername(userDTO.getUsername()) != null)
			return new AuthResponse(null, "Username Is Already Used", false, null);
		User newUser = new User();
		newUser.setUsername(userDTO.getUsername());
		newUser.setEmail(userDTO.getEmail());
		newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		newUser.setRole(userDTO.getRole() != null ? userDTO.getRole() : "USER");
		userRepository.save(newUser);
		Authentication authentication = authenticate(userDTO.getEmail(), userDTO.getPassword());
		String token = jwtProvider.generateToken(authentication);
		return new AuthResponse(token, "Register Success", true, newUser.getUsername());
	}

	public AuthResponse loginUser(UserLoginDTO loginDTO) {
		Authentication authentication = authenticate(loginDTO.getEmail(), loginDTO.getPassword());
		String token = jwtProvider.generateToken(authentication);
		return new AuthResponse(token, "Login Success", true, loginDTO.getEmail());
	}

	private Authentication authenticate(String email, String password) {
		UserDetails userDetails = userServiceImpl.loadUserByEmail(email);
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

	// Methods for managing users
	public User saveUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}
}
