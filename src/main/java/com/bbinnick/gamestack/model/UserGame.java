package com.bbinnick.gamestack.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_games", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "game_id" }) })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserGame {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	@ManyToOne
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;
	private String status = "Not Started"; // Track individual status for the game, e.g. Not Started", "In Progress", "Completed".
	private Double rating;
	private LocalDate addedOn;

	@PrePersist
	protected void onCreate() {
		if (status == null || status.trim().isEmpty())
			this.status = "Not Started";
		if (addedOn == null)
			this.addedOn = LocalDate.now();
	}
}
