package com.example.recipeapi.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "recipe_images")
public class RecipeImage {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Recipe recipe;

    private String url;
    private String thumbUrl;
    private Integer width;
    private Integer height;
    private Integer orderIndex;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
