package com.bbinnick.gamestack.response;

import lombok.Data;

@Data
public class ApiResponse {
	private String message;
	private boolean status;

	public ApiResponse(String string, boolean b) {
		this.message = string;
		this.status = b;		
	}
}