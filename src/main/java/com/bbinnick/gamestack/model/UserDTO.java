package com.bbinnick.gamestack.model;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class UserDTO {
	// Data Transfer Object (DTO) for User
	private Long id;
	private String username;
	private String email;
}