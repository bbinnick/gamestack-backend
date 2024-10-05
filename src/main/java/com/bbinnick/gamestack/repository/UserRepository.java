package com.bbinnick.gamestack.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbinnick.gamestack.model.User;
 
public interface UserRepository extends JpaRepository<User, Long> {
 
}