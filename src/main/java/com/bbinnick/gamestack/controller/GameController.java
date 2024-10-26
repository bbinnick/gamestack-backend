package com.bbinnick.gamestack.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	@PostMapping("/add")
	public ResponseEntity<?> addGame(@RequestPart("game") Game game, @RequestPart("image") MultipartFile image,
			Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser))
			return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		try {
			if (game.getTitle() == null || game.getTitle().trim().isEmpty())
				return new ResponseEntity<>("Game title is required", HttpStatus.BAD_REQUEST);
			game.setImageUrl(imageService.saveImage(image));
			Game savedGame = gameService.addGame(game, userDetails.getId());
			GameDTO savedGameDTO = convertToGameDTO(savedGame);
			log.info("Game added: Title = {}, Platform = {}, Status = {}, Image URL = {}", savedGameDTO.getTitle(),
					savedGameDTO.getPlatform(), savedGameDTO.getStatus(), savedGameDTO.getImageUrl());
			return ResponseEntity.ok(savedGameDTO);
		} catch (IOException e) {
			log.error("Error saving image file", e);
			return new ResponseEntity<>("Failed to upload image", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.error("Error adding game", e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// TODO: Add try/catch for deleteGame and updateGameStatus

	// Endpoint to delete a game by its ID
	@DeleteMapping("/{gameId}")
	public ResponseEntity<Void> deleteGame(@PathVariable Long gameId, Authentication authentication) {
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		boolean isDeleted = gameService.deleteGame(gameId, userDetails.getId());
		if (isDeleted) {
			log.info("Game deleted: {}", gameId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Endpoint to update game status
	@PatchMapping("/{gameId}/status")
	public ResponseEntity<GameDTO> updateGameStatus(@PathVariable Long gameId,
			@RequestParam(required = true) String status, Authentication authentication) {
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		if (status == null || status.isEmpty())
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		Game updatedGame = gameService.updateGameStatus(gameId, userDetails.getId(), status);
		if (updatedGame != null) {
			GameDTO updatedGameDTO = convertToGameDTO(updatedGame);
			log.info("Game status updated: {}", gameId);
			return ResponseEntity.ok(updatedGameDTO);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Endpoint to get all games
	@GetMapping("/backlog")
	public ResponseEntity<List<GameDTO>> getBacklog(Authentication authentication) {
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		List<Game> games = gameService.getGamesByUserId(userDetails.getId());
	    List<GameDTO> gamesDTO = games.stream().map(this::convertToGameDTO).collect(Collectors.toList());
		return ResponseEntity.ok(gamesDTO);
	}

	// Helper method to convert Game entity to GameDTO
	private GameDTO convertToGameDTO(Game game) {
		GameDTO gameDTO = new GameDTO();
		gameDTO.setId(game.getId());
		gameDTO.setTitle(game.getTitle());
		gameDTO.setPlatform(game.getPlatform());
		gameDTO.setGenre(game.getGenre());
		gameDTO.setStatus(game.getStatus());
		gameDTO.setAddedOn(game.getAddedOn() != null ? game.getAddedOn() : LocalDate.now());
		gameDTO.setImageUrl(game.getImageUrl());
		return gameDTO;
	}
}
