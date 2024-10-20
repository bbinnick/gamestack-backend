package com.bbinnick.gamestack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bbinnick.gamestack.model.Game;
import com.bbinnick.gamestack.model.User;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

	boolean existsByTitleAndUser(String title, User user);
	
	List<Game> findByUserId(Long userId);
}
