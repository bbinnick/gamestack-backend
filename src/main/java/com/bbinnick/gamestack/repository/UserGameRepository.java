package com.bbinnick.gamestack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bbinnick.gamestack.model.UserGame;

@Repository
public interface UserGameRepository extends JpaRepository<UserGame, Long> {

	List<UserGame> findByGameId(Long gameId);

	List<UserGame> findByUserId(Long userId);

	Optional<UserGame> findByUserIdAndGameId(Long userId, Long gameId);
}
