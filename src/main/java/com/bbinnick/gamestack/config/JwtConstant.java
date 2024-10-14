package com.bbinnick.gamestack.config;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConstant {
	// Loading secret key and expiration time from the application.properties file
	@Value("${jwt.secret}")
	private String secretKey;
	@Value("${jwt.expiration}")
	private long expirationTime;

	public String getSecretKey() {
		return Base64.getEncoder().encodeToString(secretKey.getBytes());
	}

	public long getExpirationTime() {
		return expirationTime;
	}

	public static final String JWT_HEADER = "Authorization";
}
