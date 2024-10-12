package com.bbinnick.gamestack.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bbinnick.gamestack.model.Game;
import com.bbinnick.gamestack.service.GameService;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping("/add")
    public ResponseEntity<Game> addGame(@RequestBody Game game, Principal principal) {
        return ResponseEntity.ok(gameService.addGame(game, principal));
    }
}
