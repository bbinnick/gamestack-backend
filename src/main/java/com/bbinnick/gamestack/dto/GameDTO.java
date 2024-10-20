package com.bbinnick.gamestack.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameDTO {
	@NotNull(message = "Title cannot be null")
	@Size(min = 1, message = "Title must have at least 1 character")
	private String title;
    private String genre;
    private String releaseDate;
    private String platform;
    private String imageUrl;
}
