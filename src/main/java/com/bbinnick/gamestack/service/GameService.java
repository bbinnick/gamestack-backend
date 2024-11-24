package com.bbinnick.gamestack.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bbinnick.gamestack.dto.GameDTO;
import com.bbinnick.gamestack.dto.IgdbGameDTO;
import com.bbinnick.gamestack.dto.UserGameDTO;
import com.bbinnick.gamestack.model.Game;
import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.model.UserGame;
import com.bbinnick.gamestack.repository.GameRepository;
import com.bbinnick.gamestack.repository.UserGameRepository;
import com.bbinnick.gamestack.repository.UserRepository;

@Service
public class GameService {

	private final GameRepository gameRepository;
	private final UserRepository userRepository;
	private final UserGameRepository userGameRepository;

	@Autowired
	public GameService(GameRepository gameRepository, UserRepository userRepository,
			UserGameRepository userGameRepository) {
		this.gameRepository = gameRepository;
		this.userRepository = userRepository;
		this.userGameRepository = userGameRepository;
	}

	public Game addGame(Game game) {
		return gameRepository.save(game);
	}

	// Method to add a manually created game to a specific user's backlog
	public void addGameToUserBacklog(Long gameId, Long userId) {
		Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
		if (!userGameRepository.findByUserIdAndGameId(userId, gameId).isPresent()) {
			User user = getUserById(userId);
			UserGame userGame = new UserGame();
			userGame.setUser(user);
			userGame.setGame(game);
			userGame.setStatus("Not Started");
			userGameRepository.save(userGame);
		}
	}

	// Method to add an IGDB game to the database
	public GameDTO addIgdbGameToUserBacklog(IgdbGameDTO igdbGameDTO, Long userId) {
		Game game = gameRepository.findByIgdbGameId(igdbGameDTO.getId()).orElseGet(() -> {
			Game newGame = new Game();
			newGame.setIgdbGameId(igdbGameDTO.getId());
			newGame.setTitle(igdbGameDTO.getName());
			newGame.setPlatforms(igdbGameDTO.getPlatforms());
			newGame.setGenres(igdbGameDTO.getGenres());
			newGame.setImageUrl(
					"https://images.igdb.com/igdb/image/upload/t_cover_big/" + igdbGameDTO.getCoverUrl() + ".jpg");
			return gameRepository.save(newGame);
		});

		if (userGameRepository.findByUserIdAndGameId(userId, game.getId()).isPresent())
			throw new IllegalStateException("Game is already in your backlog");
		
		userGameRepository.findByUserIdAndGameId(userId, game.getId()).orElseGet(() -> {
			User user = getUserById(userId);
			UserGame userGame = new UserGame();
			userGame.setUser(user);
			userGame.setGame(game);
			userGame.setStatus("Not Started");
			return userGameRepository.save(userGame);
		});
		return convertToGameDTO(game);
	}

	public Game editGame(Long gameId, Game game) {
		Game editedGame = gameRepository.findById(gameId)
				.orElseThrow(() -> new IllegalArgumentException("Game not found"));
		editedGame.setTitle(game.getTitle());
		editedGame.setPlatforms(game.getPlatforms());
		editedGame.setGenres(game.getGenres());
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
		Optional<UserGame> optionalUserGame = userGameRepository.findByUserIdAndGameId(userId, gameId);
		if (optionalUserGame.isPresent()) {
			userGameRepository.delete(optionalUserGame.get());
			return true;
		}
		return false;
	}

	// Method to update a game's status in a user's backlog
	public boolean updateGameStatus(Long gameId, Long userId, String status) {
		Optional<UserGame> optionalUserGame = userGameRepository.findByUserIdAndGameId(userId, gameId);
		if (optionalUserGame.isPresent()) {
			UserGame userGame = optionalUserGame.get();
			userGame.setStatus(status);
			userGameRepository.save(userGame);
			return true;
		}
		return false;
	}

	// Method to update a game's rating in a user's backlog
	// Currently doesn't rate igdb games
    public boolean updateGameRating(Long gameId, Long userId, Double rating) {
        Optional<UserGame> optionalUserGame = userGameRepository.findByUserIdAndGameId(userId, gameId);
        UserGame userGame;
        if (optionalUserGame.isPresent()) {
            userGame = optionalUserGame.get();
        } else {
            // Add the game to the user's backlog if not already present
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found"));
            User user = getUserById(userId);
            userGame = new UserGame();
            userGame.setUser(user);
            userGame.setGame(game);
            userGame.setStatus("Not Started");
        }
        userGame.setRating(rating);
        userGameRepository.save(userGame);
        return true;
    }

	public Optional<Double> getUserRating(Long gameId, Long userId) {
		Optional<UserGame> optionalUserGame = userGameRepository.findByUserIdAndGameId(userId, gameId);
		return optionalUserGame.map(UserGame::getRating);
	}

	// List all games with the users who have them in their backlog - not sure if
	// this is needed
	public List<GameDTO> listAllGamesWithUsers() {
		return gameRepository.findAll().stream().map(game -> {
			GameDTO dto = new GameDTO();
			dto.setId(game.getId());
			dto.setIgdbGameId(game.getIgdbGameId());
			dto.setTitle(game.getTitle());
			dto.setPlatforms(game.getPlatforms());
			dto.setGenres(game.getGenres());
			dto.setImageUrl(game.getImageUrl());
			dto.setUserGames(userGameRepository.findByGameId(game.getId()).stream().map(this::convertToUserGameDTO)
					.collect(Collectors.toList()));
			return dto;
		}).collect(Collectors.toList());
	}

	public List<Game> getAllGames() {
		return gameRepository.findAll().stream().map(game -> {
			game.setUserGames(null);
			return game;
		}).collect(Collectors.toList());
	}

	public Game getGameById(Long gameId) {
		return gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
	}

	public List<Game> getGamesByUserId(Long userId) {
		return userGameRepository.findByUserId(userId).stream().map(UserGame::getGame).collect(Collectors.toList());
	}

	// Method to check if a game is in a user's backlog
	public boolean isGameInUserBacklog(Long gameId, Long userId) {
		return userGameRepository.findByUserIdAndGameId(userId, gameId).isPresent();
	}

	// Method to check if a game is in the database
	public boolean isGameInDatabase(Long igdbGameId) {
		return gameRepository.findByIgdbGameId(igdbGameId).isPresent();
	}

	// Helper method to get the current user
	public User getUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
	}

	// Helper method to convert Game entity to GameDTO
	public GameDTO convertToGameDTO(Game game) {
		GameDTO gameDTO = new GameDTO();
		gameDTO.setId(game.getId());
		gameDTO.setIgdbGameId(game.getIgdbGameId());
		gameDTO.setTitle(game.getTitle());
		gameDTO.setPlatforms(game.getPlatforms());
		gameDTO.setGenres(game.getGenres());
		gameDTO.setImageUrl(game.getImageUrl());
		gameDTO.setUserGames(userGameRepository.findByGameId(game.getId()).stream().map(this::convertToUserGameDTO)
				.collect(Collectors.toList()));
		return gameDTO;
	}

	// Helper method to convert UserGame entity to UserGameDTO
	public UserGameDTO convertToUserGameDTO(UserGame userGame) {
		UserGameDTO userGameDTO = new UserGameDTO();
		userGameDTO.setUserId(userGame.getUser().getId());
		userGameDTO.setUsername(userGame.getUser().getUsername());
		userGameDTO.setStatus(userGame.getStatus());
		userGameDTO.setRating(userGame.getRating());
		userGameDTO.setAddedOn(userGame.getAddedOn());
		return userGameDTO;
	}
}
