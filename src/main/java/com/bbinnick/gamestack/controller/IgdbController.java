package com.bbinnick.gamestack.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bbinnick.gamestack.service.IgdbService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/igdb")
public class IgdbController {

	private final IgdbService igdbService;

	public IgdbController(IgdbService igdbService) {
		this.igdbService = igdbService;
	}

	@PostMapping("/games")
	public Mono<ResponseEntity<String>> getGames(@RequestBody String query) {
		return igdbService.getGames(query).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/games/{gameId}")
	public Mono<ResponseEntity<String>> getGameById(@PathVariable Long gameId) {
		return igdbService.getGameById(gameId).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/games/search")
	public Mono<ResponseEntity<String>> searchGames(@RequestParam String query, @RequestParam int limit) {
		return igdbService.searchGames(query, limit).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/games/popular")
	public Mono<ResponseEntity<String>> getPopularGames(@RequestParam String[] fields, @RequestParam int limit) {
	    return igdbService.getPopularGames(fields, limit).map(ResponseEntity::ok)
	            .defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@GetMapping("/games/hyped")
	public Mono<ResponseEntity<String>> getPopularHypedGames(@RequestParam int limit) {
	    return igdbService.getPopularHypedGames(limit)
	            .map(ResponseEntity::ok)
	            .defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/games/new-releases")
	public Mono<ResponseEntity<String>> getNewReleases(@RequestParam String[] fields, @RequestParam int limit) {
	    return igdbService.getNewReleases(fields, limit).map(ResponseEntity::ok)
	            .defaultIfEmpty(ResponseEntity.notFound().build());
	}
}
