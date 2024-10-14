package com.bbinnick.gamestack.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
		String roles = populateAuthorities(authorities);
		return Jwts.builder().issuedAt(new Date())
				.expiration(new Date(new Date().getTime() + jwtConstant.getExpirationTime()))
				.claim("username", auth.getName()).claim("authorities", roles).signWith(key).compact();
	}

	private String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
		Set<String> auths = new HashSet<>();
		for (GrantedAuthority authority : authorities) {
			auths.add(authority.getAuthority());
		}
		return String.join(",", auths);
	}

	public String getEmailFromJwtToken(String jwt) {
		jwt = jwt.substring(7);
		try {
			Claims claims = Jwts.parser().decryptWith(key).build().parseSignedClaims(jwt).getPayload();
			String email = String.valueOf(claims.get("email"));
			log.info("Email extracted from JWT: {}", claims);
			return email;
		} catch (Exception e) {
			log.error("Error extracting email from JWT: {}", e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}
