package com.bbinnick.gamestack.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.bbinnick.gamestack.auth.SecurityUser;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Component
public class JwtProvider {

	private final JwtConstant jwtConstant;
	private SecretKey key;

	@Autowired
	public JwtProvider(JwtConstant jwtConstant) {
		this.jwtConstant = jwtConstant;
		this.key = Keys.hmacShaKeyFor(jwtConstant.getSecretKey().getBytes());
	}

	public String generateToken(Authentication auth) {
		SecurityUser userDetails = (SecurityUser) auth.getPrincipal();
		Map<String, Object> claims = new HashMap<>();
		claims.put("username", userDetails.getUsername());
		claims.put("user_id", userDetails.getId());
		claims.put("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(",")));

		return Jwts.builder().claims(claims).subject(userDetails.getUsername()).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + jwtConstant.getExpirationTime())).signWith(key)
				.compact();
	}
}
