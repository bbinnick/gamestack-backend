package com.bbinnick.gamestack.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbinnick.gamestack.auth.SecurityUser;
import com.bbinnick.gamestack.dto.GameDTO;
import com.bbinnick.gamestack.model.Game;
import com.bbinnick.gamestack.service.GameService;
import com.bbinnick.gamestack.service.ImageService;

import io.jsonwebtoken.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/games")
public class GameController {

	private final GameService gameService;
	private final ImageService imageService;

	@Autowired
	public GameController(GameService gameService, ImageService imageService) {
		this.gameService = gameService;
		this.imageService = imageService;
	}

	// Endpoint to add a game
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/add")
	public ResponseEntity<?> addGame(@RequestPart("game") Game game,
			@RequestPart(value = "image", required = false) MultipartFile image) {
		try {
			if (game.getTitle() == null || game.getTitle().trim().isEmpty())
				return new ResponseEntity<>("Game title is required", HttpStatus.BAD_REQUEST);
			game.setImageUrl(imageService.saveImage(image));
			Game savedGame = gameService.addGame(game);
			GameDTO savedGameDTO = gameService.convertToGameDTO(savedGame);
			log.info("Game added: Title = {}, Platform = {}, Genre = {}, Image URL = {}", savedGameDTO.getTitle(),
					savedGameDTO.getPlatform(), savedGameDTO.getGenre(), savedGameDTO.getImageUrl());
			return ResponseEntity.ok(savedGameDTO);
		} catch (IOException e) {
			log.error("Error saving image file", e);
			return new ResponseEntity<>("Failed to upload image", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Error adding game", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Endpoint to add a game to a user's backlog
	@PostMapping("/add-to-backlog/{gameId}")
	public ResponseEntity<?> addToBacklog(@PathVariable Long gameId, Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser))
			return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		try {
			gameService.addGameToUserBacklog(gameId, userDetails.getId());
			log.info("Game added to backlog: {}", gameId);
			return new ResponseEntity<>("Game added to backlog", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/{gameId}")
	public ResponseEntity<?> editGame(@PathVariable Long gameId, @RequestPart("game") Game game,
			@RequestPart(value = "image", required = false) MultipartFile image) {
		try {
			if (image != null && !image.isEmpty())
				game.setImageUrl(imageService.saveImage(image));
			else {
				Game existingGame = gameService.getGameById(gameId);
				game.setImageUrl(existingGame.getImageUrl());
			}
			Game editedGame = gameService.editGame(gameId, game);
			GameDTO editedGameDTO = gameService.convertToGameDTO(editedGame);
			log.info("Game edited: Title = {}, Platform = {}, Genre = {}, Image URL = {}", editedGameDTO.getTitle(),
					editedGameDTO.getPlatform(), editedGameDTO.getGenre(), editedGameDTO.getImageUrl());
			return ResponseEntity.ok(editedGameDTO);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// Endpoint to delete a game by its ID (Admin only)
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{gameId}")
	public ResponseEntity<Void> deleteGame(@PathVariable Long gameId) {
		boolean isDeleted = gameService.deleteGameById(gameId);
		if (isDeleted) {
			log.info("Game deleted: {}", gameId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Endpoint to remove a game from a user's backlog
	@DeleteMapping("/remove-from-backlog/{gameId}")
	public ResponseEntity<Void> removeFromBacklog(@PathVariable Long gameId, Authentication authentication) {
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		boolean isRemoved = gameService.removeGameFromUserBacklog(gameId, userDetails.getId());
		if (isRemoved) {
			log.info("Game removed from backlog: {}", gameId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Endpoint to update game status
	@PatchMapping("/{gameId}/status")
	public ResponseEntity<Void> updateGameStatus(@PathVariable Long gameId,
			@RequestParam(required = true) String status, Authentication authentication) {
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		if (status == null || status.isEmpty())
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		boolean isUpdated = gameService.updateGameStatus(gameId, userDetails.getId(), status);
		if (isUpdated) {
			log.info("Game status updated: {}", gameId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Endpoint to rate a game
	@PostMapping("/{gameId}/rate")
	public ResponseEntity<?> rateGame(@PathVariable Long gameId, @RequestParam Double rating,
			Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser))
			return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		try {
			boolean isRated = gameService.updateGameRating(gameId, userDetails.getId(), rating);
			if (isRated) {
				log.info("Game rated: {} with rating {}", gameId, rating);
				return new ResponseEntity<>("Game rated successfully", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Game not found in user's backlog", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/{gameId}/user-rating")
	public ResponseEntity<?> getUserRating(@PathVariable Long gameId, Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser))
			return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		Optional<Double> rating = gameService.getUserRating(gameId, userDetails.getId());
		if (rating.isPresent() && rating.get() != null) {
			return new ResponseEntity<>(Collections.singletonMap("rating", rating.get()), HttpStatus.OK);
		}
		return new ResponseEntity<>(Collections.singletonMap("message", "Game has not been rated yet"), HttpStatus.OK);
	}

	// Endpoint to show a single user's games
	@GetMapping("/backlog")
	public ResponseEntity<List<GameDTO>> getBacklog(Authentication authentication) {
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		List<Game> games = gameService.getGamesByUserId(userDetails.getId());
		List<GameDTO> gamesDTO = games.stream().map(gameService::convertToGameDTO).collect(Collectors.toList());
		return ResponseEntity.ok(gamesDTO);
	}

	// Endpoint to show a game's details
	@GetMapping("/{gameId}")
	public ResponseEntity<GameDTO> getGameDetails(@PathVariable Long gameId) {
		Game game = gameService.getGameById(gameId);
		GameDTO gameDTO = gameService.convertToGameDTO(game);
		return ResponseEntity.ok(gameDTO);
	}

	// Endpoint to list all games
	@GetMapping("/all")
	public ResponseEntity<List<Game>> listAllGames() {
		List<Game> games = gameService.getAllGames();
		return ResponseEntity.ok(games);
	}

	// Endpoint to list all users profiles - not sure if this is needed
	@GetMapping("/all-with-users")
	public ResponseEntity<List<GameDTO>> listAllGamesWithUsers() {
		List<GameDTO> gamesWithUsers = gameService.listAllGamesWithUsers();
		return ResponseEntity.ok(gamesWithUsers);
	}
}
