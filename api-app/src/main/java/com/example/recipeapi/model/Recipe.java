package com.example.recipeapi.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String externalId; // message id for idempotency

    @ManyToOne(optional = false)
    private Chef chef;

    private String title;

    @Column(length = 2000)
    private String summary;

    @Lob
    private String ingredientsJson;

    @Lob
    private String stepsJson;

    @ElementCollection
    private List<String> labels = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private State state = State.DRAFT;

    private Instant createdAt = Instant.now();

    private Instant publishedAt;

    public enum State { DRAFT, PUBLISHED }

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public Chef getChef() { return chef; }
    public void setChef(Chef chef) { this.chef = chef; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getIngredientsJson() { return ingredientsJson; }
    public void setIngredientsJson(String ingredientsJson) { this.ingredientsJson = ingredientsJson; }
    public String getStepsJson() { return stepsJson; }
    public void setStepsJson(String stepsJson) { this.stepsJson = stepsJson; }
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
}
