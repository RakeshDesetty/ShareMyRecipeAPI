package com.example.recipeapi.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="refresh_tokens")
public class RefreshToken {
    @Id @GeneratedValue
    private UUID id;
    @ManyToOne(optional=false) private Chef chef;
    @Column(nullable=false) private String tokenHash;
    private Instant expiresAt;
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Chef getChef() { return chef; }
    public void setChef(Chef chef) { this.chef = chef; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
