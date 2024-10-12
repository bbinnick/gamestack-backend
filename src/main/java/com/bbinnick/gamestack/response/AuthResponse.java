package com.bbinnick.gamestack.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthResponse {
	private String jwt;
	private String message;
	private Boolean status;
	private String username;
}
