package com.bbinnick.gamestack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bbinnick.gamestack.auth.SecurityUser;
import com.bbinnick.gamestack.model.Game;
import com.bbinnick.gamestack.model.User;
import com.bbinnick.gamestack.repository.GameRepository;
import com.bbinnick.gamestack.repository.UserRepository;

@RestController
@RequestMapping("/games")
public class GameController {

	@Autowired
	private GameRepository gameRepository;

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/add")
	public ResponseEntity<Game> addGame(@RequestBody Game game, Authentication authentication) {
		SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
		User currentUser = userRepository.findById(userDetails.getId()).orElse(null);
		if (currentUser == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		game.setUser(currentUser);
		Game savedGame = gameRepository.save(game);

		return ResponseEntity.ok(savedGame);
	}
	/*
	 * @PostMapping("/add") public ResponseEntity<Game> addGame(@RequestBody Game
	 * game, Principal principal) { if (principal == null) { return new
	 * ResponseEntity<>(HttpStatus.UNAUTHORIZED); } // Fetch the logged-in user from
	 * the database using the principal's name (username or email) User user =
	 * userRepository.findByUsername(principal.getName()); if (user == null) {
	 * return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); } // Set the user for
	 * the game game.setUser(user); // Save the game to the repository Game
	 * savedGame = gameRepository.save(game); return new ResponseEntity<>(savedGame,
	 * HttpStatus.CREATED); }
	 */
}
