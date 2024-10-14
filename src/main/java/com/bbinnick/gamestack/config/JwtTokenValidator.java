package com.bbinnick.gamestack.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenValidator extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;

	@Autowired
	public JwtTokenValidator(JwtProvider jwtProvider) {
		this.jwtProvider = jwtProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String jwt = request.getHeader(JwtConstant.JWT_HEADER);
		if (jwt != null && jwt.startsWith("Bearer ")) {
			jwt = jwt.substring(7);
			try {
				SecretKey key = jwtProvider.getKey();
				Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt).getPayload();
				String username = String.valueOf(claims.get("username")); // Ensure the username is part of the claims
				log.info("Claims in JwtTokenValidator: {}", claims);
				// Handle expiration
				if (claims.getExpiration().before(new Date())) {
					throw new BadCredentialsException("Token expired");
				}
				List<GrantedAuthority> authorities = AuthorityUtils
						.commaSeparatedStringToAuthorityList((String) claims.get("authorities"));
				Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (Exception e) {
				log.error("Invalid token: {}", e.getMessage());
				throw new BadCredentialsException("Invalid token", e);
			}
		}
		filterChain.doFilter(request, response);
	}
}
