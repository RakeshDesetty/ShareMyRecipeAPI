package com.example.recipeapi.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="follows", uniqueConstraints=@UniqueConstraint(columnNames = {"follower_id","followee_id"}))
public class Follow {
    @Id @GeneratedValue
    private UUID id;
    @ManyToOne(optional=false) private Chef follower;
    @ManyToOne(optional=false) private Chef followee;
    private Instant createdAt = Instant.now();
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Chef getFollower() { return follower; }
    public void setFollower(Chef follower) { this.follower = follower; }
    public Chef getFollowee() { return followee; }
    public void setFollowee(Chef followee) { this.followee = followee; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
