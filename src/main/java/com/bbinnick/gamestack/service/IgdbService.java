package com.bbinnick.gamestack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.bbinnick.gamestack.utils.IgdbQueryBuilder;

import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IgdbService {

	private final WebClient webClient;

	@Value("${igdb.client-id}")
	private String clientId;

	@Value("${igdb.access-token}")
	private String accessToken;

	public IgdbService(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("https://api.igdb.com/v4").build();
	}

	public Mono<String> getGames(String query) {
		try {
			return webClient.post().uri("/games").header("Client-ID", clientId)
					.header("Authorization", "Bearer " + accessToken).bodyValue(query).retrieve()
					.onStatus(HttpStatusCode::is4xxClientError, response -> {
						log.error("Client error: {}", response.statusCode());
						return Mono.error(new RuntimeException("Client error: " + response.statusCode()));
					}).onStatus(HttpStatusCode::is5xxServerError, response -> {
						log.error("Server error: {}", response.statusCode());
						return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
					}).bodyToMono(String.class).doOnError(e -> log.error("Error fetching games from IGDB", e));
		} catch (WebClientResponseException e) {
			log.error("Error getting games from IGDB", e);
			throw new RuntimeException("Error getting games from IGDB", e);
		}
	}

	// Fetch game by ID and return details. fix fields to be more specific.
	public Mono<String> getGameById(Long gameId) {
		String query = String.format("fields id, name, cover.image_id, platforms.name, genres.name, rating, summary; where id = %d;", gameId);
		return getGames(query);
	}

	// Search for games by name
	public Mono<String> searchGames(String searchQuery, int limit) {
		String query = IgdbQueryBuilder.buildSearchGameQuery(new String[] { "*" }, limit)
				+ String.format(" search \"%s\";", searchQuery);
		return getGames(query);
	}

	// Fetch popular games (using helper method for consistency)
	public Mono<String> getPopularGames(int limit) {
		String query = IgdbQueryBuilder.buildPopularGamesQuery(
				new String[] { "id", "name", "cover.image_id", "hypes", "rating" },
				limit);
		return getGames(query);
	}

	// doesnt work
	public Mono<String> getTopPlayingGames() {
		String query = """
				    fields game_id, value, popularity_type;
				    where popularity_type = 3;
				    sort value desc;
				    limit 10;
				""";
		return getGames(query);
	}

	// Fetch new releases. Replace with recent critic reviews or another category.
	// weird games show up.
	public Mono<String> getNewReleases(int limit) {
		String query = IgdbQueryBuilder.buildNewReleasesQuery(new String[] { "id", "name", "cover.image_id" }, limit);
		return getGames(query);
	}

}