package com.bbinnick.gamestack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@PostMapping("/add")
	public ResponseEntity<?> addGame(@RequestBody Game game, Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser))
			return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		try {
			if (game.getTitle() == null || game.getTitle().trim().isEmpty())
				return new ResponseEntity<>("Game title is required", HttpStatus.BAD_REQUEST);
			Game savedGame = gameService.addGame(game, userDetails.getId());
			log.info("Game added: {}", savedGame);
			return ResponseEntity.ok(savedGame);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/backlog")
	public ResponseEntity<List<Game>> getBacklog(Authentication authentication) {
	    SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
	    List<Game> games = gameService.getGamesByUserId(userDetails.getId());
	    return ResponseEntity.ok(games);
	}

}
