package com.bbinnick.gamestack.auth;

import com.bbinnick.gamestack.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

public class SecurityUser implements UserDetails {

	private static final long serialVersionUID = 1L;
	private final User user;

	public SecurityUser(User user) {
		this.user = user;
	}

	public Long getId() {
		return user.getId();
	}

	// Expose other fields as needed
	public String getEmail() {
		return user.getEmail();
	}

	public String getRole() {
		return user.getRole();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getAuthorities();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
