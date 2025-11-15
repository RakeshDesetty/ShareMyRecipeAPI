package com.example.recipeapi.repository;

import com.example.recipeapi.model.Follow;
import com.example.recipeapi.model.Chef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Extension possibility -- OPEN - CLOSED Principle
public interface FollowRepository extends JpaRepository<Follow, UUID> {
    boolean existsByFollowerAndFollowee(Chef follower, Chef followee);
    Optional<Follow> findByFollowerAndFollowee(Chef follower, Chef followee);
    List<Follow> findByFollower(Chef follower);
}
