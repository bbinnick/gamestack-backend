package com.bbinnick.gamestack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bbinnick.gamestack.model.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

	Optional<Game> findByIgdbGameId(Long igdbGameId);
}
