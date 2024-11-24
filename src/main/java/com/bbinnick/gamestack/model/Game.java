package com.bbinnick.gamestack.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Game {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long igdbGameId;
	private String title;
	@ElementCollection
	@CollectionTable(name = "game_platforms", joinColumns = @JoinColumn(name = "game_id"))
	private List<String> platforms = new ArrayList<>();
	@ElementCollection
	@CollectionTable(name = "game_genres", joinColumns = @JoinColumn(name = "game_id"))
	private List<String> genres = new ArrayList<>();
	private String imageUrl;

	@OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
	private List<UserGame> userGames = new ArrayList<>();

}
