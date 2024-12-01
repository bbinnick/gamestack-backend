package com.bbinnick.gamestack.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bbinnick.gamestack.dto.IgdbGameDTO;
import com.bbinnick.gamestack.service.IgdbService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@GetMapping("/games/details/{gameId}")
	public Mono<ResponseEntity<IgdbGameDTO>> getIgdbGameDetails(@PathVariable Long gameId) {
		return igdbService.getGameById(gameId).map(response -> {
			IgdbGameDTO igdbGameDTO = parseIgdbGameResponse(response);
			return ResponseEntity.ok(igdbGameDTO);
		}).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/games/search")
	public Mono<ResponseEntity<String>> searchGames(@RequestParam String query, @RequestParam int limit) {
		return igdbService.searchGames(query, limit).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/games/popular")
	public Mono<ResponseEntity<String>> getPopularGames(@RequestParam int limit) {
		return igdbService.getPopularGames(limit).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/games/new-releases")
	public Mono<ResponseEntity<String>> getNewReleases(@RequestParam int limit) {
		return igdbService.getNewReleases(limit).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/games/topPlaying")
	public Mono<ResponseEntity<String>> getTopPlaying(@RequestParam int limit) {
		return igdbService.getTopPlayingGames().map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	private IgdbGameDTO parseIgdbGameResponse(String response) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode rootNode = objectMapper.readTree(response).get(0);
			IgdbGameDTO igdbGameDTO = new IgdbGameDTO();
			igdbGameDTO.setId(rootNode.path("id").asLong());
			igdbGameDTO.setName(rootNode.path("name").asText());
			igdbGameDTO.setCoverUrl(rootNode.path("cover").path("image_id").asText());
			igdbGameDTO.setRating(rootNode.path("rating").asDouble());
			igdbGameDTO.setSummary(rootNode.path("summary").asText());

			List<String> platforms = new ArrayList<>();
			rootNode.path("platforms").forEach(platform -> platforms.add(platform.path("name").asText()));
			igdbGameDTO.setPlatforms(platforms);

			List<String> genres = new ArrayList<>();
			rootNode.path("genres").forEach(genre -> genres.add(genre.path("name").asText()));
			igdbGameDTO.setGenres(genres);

			return igdbGameDTO;
		} catch (Exception e) {
			throw new RuntimeException("Error parsing IGDB game response", e);
		}
	}

}
