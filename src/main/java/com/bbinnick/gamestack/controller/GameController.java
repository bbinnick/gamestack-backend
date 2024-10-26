package com.bbinnick.gamestack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbinnick.gamestack.auth.SecurityUser;
import com.bbinnick.gamestack.model.Game;
import com.bbinnick.gamestack.service.GameService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/games")
public class GameController {

	private final GameService gameService;

	@Autowired
	public GameController(GameService gameService) {
		this.gameService = gameService;
	}

	// Endpoint to add a game
	@PostMapping("/add")
	public ResponseEntity<?> addGame(@RequestBody Game game, Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser))
			return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		try {
			if (game.getTitle() == null || game.getTitle().trim().isEmpty())
				return new ResponseEntity<>("Game title is required", HttpStatus.BAD_REQUEST);
			Game savedGame = gameService.addGame(game, userDetails.getId());
			log.info("Game status updated: Title = {}, Platform = {}, Status = {}", game.getTitle(), game.getPlatform(), game.getStatus());
			return ResponseEntity.ok(savedGame);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	//TODO: Add try/catch for deleteGame and updateGameStatus
	
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
	public ResponseEntity<Game> updateGameStatus(@PathVariable Long gameId,
			@RequestParam(required = true) String status, Authentication authentication) {
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		if (status == null || status.isEmpty())
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		Game updatedGame = gameService.updateGameStatus(gameId, userDetails.getId(), status);
		if (updatedGame != null) {
			//log.info("Game status updated: Title = {}, Platform = {}, Status = {}", game.getTitle(), game.getPlatform(), game.getStatus());
			log.info("Game status updated: {}", gameId);
			return ResponseEntity.ok(updatedGame);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Endpoint to get all games
	@GetMapping("/backlog")
	public ResponseEntity<List<Game>> getBacklog(Authentication authentication) {
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		List<Game> games = gameService.getGamesByUserId(userDetails.getId());
		return ResponseEntity.ok(games);
	}
}
