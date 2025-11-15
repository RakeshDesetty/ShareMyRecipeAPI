package com.example.recipeapi.repository;

import com.example.recipeapi.model.Chef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// Can be extended further depending on the functionality requirement. -- OPEN - CLOSED Principle
public interface ChefRepository extends JpaRepository<Chef, UUID> {
    Optional<Chef> findByEmail(String email);
    Optional<Chef> findByHandle(String handle);
}
