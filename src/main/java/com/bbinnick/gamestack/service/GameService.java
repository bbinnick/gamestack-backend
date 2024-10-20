package com.bbinnick.gamestack.service;

import java.util.List;
import java.util.Optional;

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
		User user = getUserById(userId);
		if (game.getTitle() == null || game.getTitle().trim().isEmpty())
			throw new IllegalArgumentException("Game title is required");
		game.setUser(user);
		return gameRepository.save(game);
	}

	// Delete a game by ID, checking if the game belongs to the authenticated user
	public boolean deleteGame(Long gameId, Long userId) {
		Optional<Game> optionalGame = gameRepository.findById(gameId);
		if (optionalGame.isPresent()) {
			Game game = optionalGame.get();
			if (game.getUser().getId().equals(userId)) {
				gameRepository.delete(game);
				return true;
			}
		}
		return false;
	}

	// Update the status of a game, ensuring it belongs to the authenticated user
	public Game updateGameStatus(Long gameId, Long userId, String status) {
		Optional<Game> optionalGame = gameRepository.findById(gameId);
		if (optionalGame.isPresent()) {
			Game game = optionalGame.get();
			if (game.getUser().getId().equals(userId)) {
				game.setStatus(status);
				return gameRepository.save(game);
			}
		}
		return null;
	}

	public List<Game> getGamesByUserId(Long userId) {
		return gameRepository.findByUserId(userId);
	}

	// Helper method to get the current user
	public User getUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
	}
}
