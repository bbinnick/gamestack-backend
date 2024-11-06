package com.bbinnick.gamestack.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 20)
	private String role = "USER";
	@Column(nullable = false, length = 45, unique = true)
	private String email;
	@Column(nullable = false, length = 64)
	private String password;
	@Column(nullable = false, length = 20)
	private String username;
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@JsonManagedReference // to prevent infinite loop when serializing
	private List<Game> games = new ArrayList<>();
	@ManyToMany
	@JoinTable(name = "user_games", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "game_id"))
	private List<Game> gamesInBacklog = new ArrayList<>();

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.toUpperCase()));
	}
}