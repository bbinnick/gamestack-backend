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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

}