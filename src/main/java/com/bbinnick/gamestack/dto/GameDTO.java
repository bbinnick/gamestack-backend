package com.bbinnick.gamestack.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameDTO {
	private Long id;
	@NotNull(message = "Title cannot be null")
	@Size(min = 1, message = "Title must have at least 1 character")
	private String title;
    private List<String> platforms;
    private List<String> genres;
	private String imageUrl;
	private List<UserGameDTO> userGames;
}
