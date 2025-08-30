package com.bbinnick.gamestack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.bbinnick.gamestack.utils.IgdbQueryBuilder;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IgdbService {

	private final WebClient webClient;
	private final WebClient authClient;

	@Value("${igdb.client-id}")
	private String clientId;

	@Value("${igdb.client-secret}")
	private String clientSecret;

	@Value("${igdb.access-token:}")
	private String accessToken;

	public IgdbService(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("https://api.igdb.com/v4").build();
		this.authClient = webClientBuilder.baseUrl("https://id.twitch.tv/oauth2").build();
	}

	// small sentinel exception to trigger refresh+retry
	private static class TokenRefreshException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private Mono<Void> refreshAccessToken() {
		log.info("Refreshing IGDB access token via Twitch...");
		return authClient.post()
				.uri(uriBuilder -> uriBuilder.path("/token").queryParam("client_id", clientId)
						.queryParam("client_secret", clientSecret).queryParam("grant_type", "client_credentials")
						.build())
				.retrieve().bodyToMono(JsonNode.class).doOnNext(json -> {
					if (json.has("access_token")) {
						this.accessToken = json.get("access_token").asText();
						log.info("Obtained new IGDB access token");
					} else {
						log.error("No access_token in Twitch response: {}", json);
						throw new RuntimeException("Failed to obtain access_token from Twitch");
					}
				}).then();
	}

	public Mono<String> getGames(String query) {
		return doGamesRequest(query).onErrorResume(err -> {
			// If TokenRefreshException, perform token refresh then retry once
			if (err instanceof TokenRefreshException) {
				return refreshAccessToken().then(doGamesRequest(query));
			}
			return Mono.error(err);
		}).doOnError(e -> log.error("Error fetching games from IGDB", e));
	}

	private Mono<String> doGamesRequest(String query) {
		try {
			return webClient.post().uri("/games").header("Client-ID", clientId)
					.header("Authorization", "Bearer " + (accessToken == null ? "" : accessToken)).bodyValue(query)
					.retrieve().onStatus(HttpStatusCode::is4xxClientError, response -> {
						if (response.statusCode().value() == 401) {
							log.warn("IGDB returned 401 - will refresh token and retry");
							return Mono.error(new TokenRefreshException());
						}
						log.error("Client error: {}", response.statusCode());
						return Mono.error(new RuntimeException("Client error: " + response.statusCode()));
					}).onStatus(HttpStatusCode::is5xxServerError, response -> {
						log.error("Server error: {}", response.statusCode());
						return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
					}).bodyToMono(String.class);
		} catch (WebClientResponseException e) {
			log.error("Error getting games from IGDB", e);
			throw new RuntimeException("Error getting games from IGDB", e);
		}
	}

	// Fetch game by ID and return details. fix fields to be more specific.
	public Mono<String> getGameById(Long gameId) {
		String query = String.format(
				"fields id, name, cover.image_id, platforms.name, genres.name, rating, summary; where id = %d;",
				gameId);
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
		String query = IgdbQueryBuilder
				.buildPopularGamesQuery(new String[] { "id", "name", "cover.image_id", "hypes", "rating" }, limit);
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