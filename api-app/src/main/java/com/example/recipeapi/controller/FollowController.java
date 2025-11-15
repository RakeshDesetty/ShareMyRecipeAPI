package com.example.recipeapi.controller;

import com.example.recipeapi.model.Chef;
import com.example.recipeapi.model.Follow;
import com.example.recipeapi.repository.ChefRepository;
import com.example.recipeapi.repository.FollowRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chef")
public class FollowController {

    private final FollowRepository followRepo;
    private final ChefRepository chefRepo;

    public FollowController(FollowRepository followRepo, ChefRepository chefRepo) {
        this.followRepo = followRepo;
        this.chefRepo = chefRepo;
    }

    @PostMapping("/{chefId}/follow")
    public ResponseEntity<?> follow(@PathVariable UUID chefId, Authentication auth) {
        if (auth==null) return ResponseEntity.status(401).body(java.util.Map.of("error","unauthenticated"));
        UUID followerId = (UUID) auth.getPrincipal();
        Chef follower = chefRepo.findById(followerId).orElseThrow();
        Chef followee = chefRepo.findById(chefId).orElseThrow();
        if (followRepo.existsByFollowerAndFollowee(follower, followee)) return ResponseEntity.ok(java.util.Map.of("status","already_following"));
        Follow f = new Follow(); f.setFollower(follower); f.setFollowee(followee);
        followRepo.save(f);
        return ResponseEntity.ok(java.util.Map.of("status","followed"));
    }

    @PostMapping("/{chefId}/unfollow")
    public ResponseEntity<?> unfollow(@PathVariable UUID chefId, Authentication auth) {
        if (auth==null) return ResponseEntity.status(401).body(java.util.Map.of("error","unauthenticated"));
        UUID followerId = (UUID) auth.getPrincipal();
        Chef follower = chefRepo.findById(followerId).orElseThrow();
        Chef followee = chefRepo.findById(chefId).orElseThrow();
        followRepo.findByFollowerAndFollowee(follower, followee).ifPresent(followRepo::delete);
        return ResponseEntity.ok(java.util.Map.of("status","unfollowed"));
    }

    @GetMapping("/following/recipes")
    public ResponseEntity<?> followingRecipes(Authentication auth,
                                              @RequestParam(defaultValue="0") int page,
                                              @RequestParam(defaultValue="20") int page_size) {
        if (auth==null) return ResponseEntity.status(401).body(java.util.Map.of("error","unauthenticated"));
        UUID followerId = (UUID) auth.getPrincipal();
        Chef follower = chefRepo.findById(followerId).orElseThrow();
        List<Follow> follows = followRepo.findByFollower(follower);
        List<java.util.UUID> ids = follows.stream().map(f -> f.getFollowee().getId()).toList();
        if (ids.isEmpty()) return ResponseEntity.ok(java.util.Map.of("data", List.of(), "meta", Map.of("page",0,"page_size",0,"total_items",0,"total_pages",0,"has_next",false,"has_prev",false)));
        Pageable pageable = PageRequest.of(Math.max(0,page), Math.min(page_size,100));
        var pageRes = ((org.springframework.data.jpa.repository.JpaRepository) ((org.springframework.data.jpa.repository.JpaRepository) ((org.springframework.data.jpa.repository.JpaRepository) null))).findAll(); // placeholder to avoid compile-time in this scaffold
        // For starter implementation, we return empty list or prompt to fetch via /api/recipes with chef_id filter.
        return ResponseEntity.ok(java.util.Map.of("data", List.of(), "meta", Map.of("page",0,"page_size",0,"total_items",0,"total_pages",0,"has_next",false,"has_prev",false)));
    }
}
