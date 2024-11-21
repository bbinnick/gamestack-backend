package com.bbinnick.gamestack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
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

	public Mono<String> getGameById(Long gameId) {
		String query = IgdbQueryBuilder.buildGameQuery(new String[] { "*" }, 1) + " where id = " + gameId + ";";
		return getGames(query);
	}

	public Mono<String> searchGames(String searchQuery, int limit) {
		String query = IgdbQueryBuilder.buildGameQuery(new String[] { "*" }, limit) + " search \"" + searchQuery
				+ "\";";
		return getGames(query);
	}

	public Mono<String> getPopularGames(String[] fields, int limit) {
		String query = IgdbQueryBuilder.buildPopularGamesQuery(fields, limit);
		return getGames(query);
	}
	// need queries to be in the body of the request, not params
	public Mono<String> getPopularHypedGames(int limit) {
		String query = """
				fields id, name, cover.url, hypes, rating;
				where hypes > 50;
				sort hypes desc;
				limit %d;
				""".formatted(limit);
		return getGames(query); // Reuse the getGames method to send this query
	}

	public Mono<String> getNewReleases(String[] fields, int limit) {
		String query = IgdbQueryBuilder.buildNewReleasesQuery(fields, limit);
		return getGames(query);
	}
}