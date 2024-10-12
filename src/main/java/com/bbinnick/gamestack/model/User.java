package com.bbinnick.gamestack.model;

import java.util.ArrayList;
import java.util.List;

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
	@Column(nullable = false, length = 20, unique = false)
	private String role = "USER";
	@Column(nullable = false, length = 45, unique = true)
	private String email;
	@Column(nullable = false, length = 64)
	private String password;
	@Column(nullable = false, length = 20)
	private String username;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Game> games = new ArrayList<>();
}