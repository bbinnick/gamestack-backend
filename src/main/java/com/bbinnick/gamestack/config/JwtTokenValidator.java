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

import com.bbinnick.gamestack.auth.SecurityUser;
import com.bbinnick.gamestack.model.User;

import javax.crypto.SecretKey;
import java.io.IOException;
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
				String username = claims.get("username", String.class);
				Long userId = claims.get("user_id", Long.class);
				String authorities = claims.get("authorities", String.class);
				List<GrantedAuthority> authList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
				User user = new User();
				user.setId(userId);
				user.setUsername(username);
				// Set other user fields if necessary
				SecurityUser userDetails = new SecurityUser(user);
				Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authList);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (Exception e) {
				log.error("Invalid token: {}", e.getMessage());
				throw new BadCredentialsException("Invalid token", e);
			}
		}
		filterChain.doFilter(request, response);
	}
}
