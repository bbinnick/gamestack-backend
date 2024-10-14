package com.bbinnick.gamestack.service;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bbinnick.gamestack.model.Game;
import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.repository.GameRepository;
import com.bbinnick.gamestack.repository.UserRepository;

@Service
public class GameService {

	@Autowired
	private GameRepository gameRepository;
	@Autowired
	private UserRepository userRepository;

	public Game addGame(Game game, Principal principal) {
		User user = userRepository.findByUsername(principal.getName());
		if (user == null) {
			throw new UsernameNotFoundException("User not found with username: " + principal.getName());
		}
		game.setUser(user);
		return gameRepository.save(game);
	}
}