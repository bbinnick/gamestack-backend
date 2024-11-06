package com.bbinnick.gamestack.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bbinnick.gamestack.dto.GameWithUsersDTO;
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

	public Game addGame(Game game) {
		return gameRepository.save(game);
	}

	// Method to add an existing game to a specific user's backlog
	public void addGameToUserBacklog(Long gameId, Long userId) {
		Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
		User user = getUserById(userId);
		// Ensure the game isn't already in the user's backlog
		if (!user.getGamesInBacklog().contains(game)) {
			user.getGamesInBacklog().add(game);
			userRepository.save(user);
		}
	}

	public Game editGame(Long gameId, Game game) {
		Game editedGame = gameRepository.findById(gameId)
				.orElseThrow(() -> new IllegalArgumentException("Game not found"));
		editedGame.setTitle(game.getTitle());
		editedGame.setPlatform(game.getPlatform());
		editedGame.setGenre(game.getGenre());
		editedGame.setStatus(game.getStatus());
		editedGame.setImageUrl(game.getImageUrl());
		return gameRepository.save(editedGame);
	}

	// Method to delete a game by its ID (Admin only)
	public boolean deleteGameById(Long gameId) {
		Optional<Game> optionalGame = gameRepository.findById(gameId);
		if (optionalGame.isPresent()) {
			gameRepository.delete(optionalGame.get());
			return true;
		}
		return false;
	}

	// Remove a game from a user's backlog without deleting it from the database
	public boolean removeGameFromUserBacklog(Long gameId, Long userId) {
		Optional<User> optionalUser = userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			Optional<Game> gameToRemove = user.getGamesInBacklog().stream().filter(game -> game.getId().equals(gameId))
					.findFirst();
			if (gameToRemove.isPresent()) {
				user.getGamesInBacklog().remove(gameToRemove.get());
				userRepository.save(user);
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
			if (game.getUser() != null && game.getUser().getId().equals(userId)) {
				game.setStatus(status);
				return gameRepository.save(game);
			}
		}
		return null;
	}

	public List<GameWithUsersDTO> listAllGamesWithUsers() {
		return gameRepository.findAll().stream().map(game -> {
			GameWithUsersDTO dto = new GameWithUsersDTO();
			dto.setId(game.getId());
			dto.setTitle(game.getTitle());
			dto.setPlatform(game.getPlatform());
			dto.setGenre(game.getGenre());
			dto.setImageUrl(game.getImageUrl());
			dto.setBacklogUsers(userRepository.findAll().stream()
					.filter(user -> user.getGamesInBacklog().contains(game)).collect(Collectors.toList()));

			return dto;
		}).collect(Collectors.toList());
	}

	public List<Game> getAllGames() {
		return gameRepository.findAll();
	}

	public Game getGameById(Long gameId) {
		return gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
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
