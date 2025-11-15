package com.example.recipeapi.repository;

import com.example.recipeapi.model.RecipeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

//Repository Layer -- Just for the contract purpose
public interface RecipeImageRepository extends JpaRepository<RecipeImage, UUID> {
}
