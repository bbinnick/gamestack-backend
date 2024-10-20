package com.bbinnick.gamestack.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bbinnick.gamestack.model.Game;
import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.repository.GameRepository;
import com.bbinnick.gamestack.repository.UserRepository;

@Service
public class GameService {

	private final GameRepository gameRepository;
	private final UserRepository userRepository;

	@Autowired
	public GameService(GameRepository gameRepository, UserRepository userRepository) {
		this.gameRepository = gameRepository;
		this.userRepository = userRepository;
	}

	public Game addGame(Game game, Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
		if (game.getTitle() == null || game.getTitle().trim().isEmpty())
			throw new IllegalArgumentException("Game title is required");
		game.setUser(user);
		return gameRepository.save(game);
	}
	
	public List<Game> getGamesByUserId(Long userId) {
	    return gameRepository.findByUserId(userId);
	}

}
