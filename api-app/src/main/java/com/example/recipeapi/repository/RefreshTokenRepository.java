package com.example.recipeapi.repository;

import com.example.recipeapi.model.RefreshToken;
import com.example.recipeapi.model.Chef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByChef(Chef chef);
    void deleteByChef(Chef chef);
}
