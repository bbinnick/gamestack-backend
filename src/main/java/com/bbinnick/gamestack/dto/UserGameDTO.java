package com.bbinnick.gamestack.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class UserGameDTO {
	private Long userId;
	private String username;
	private String status;
	private Double rating;
	private LocalDate addedOn;
}