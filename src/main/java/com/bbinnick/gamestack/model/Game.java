package com.bbinnick.gamestack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Game {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	private String platform;
	private String genre;
	private String status; // e.g., "Not Started", "In Progress", "Completed"
	private LocalDate addedOn;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonBackReference
	// @ToString.Exclude
	private User user;

	@PrePersist
	protected void onCreate() {
		if (status == null || status.trim().isEmpty())
			this.status = "Not Started";
		if (addedOn == null)
			this.addedOn = LocalDate.now();
	}
}
