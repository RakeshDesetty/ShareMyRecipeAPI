package com.example.recipeapi.controller;

import com.example.recipeapi.dto.AuthDtos.*;
import com.example.recipeapi.model.Chef;
import com.example.recipeapi.model.RefreshToken;
import com.example.recipeapi.repository.ChefRepository;
import com.example.recipeapi.repository.RefreshTokenRepository;
import com.example.recipeapi.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ChefRepository chefRepo;
    private final RefreshTokenRepository refreshRepo;
    private final JwtTokenProvider jwt;

    @Value("${app.auth.email-verification-enabled:false}")
    private boolean emailVerificationEnabled;

    public AuthController(ChefRepository chefRepo, RefreshTokenRepository refreshRepo, JwtTokenProvider jwt) {
        this.chefRepo = chefRepo;
        this.refreshRepo = refreshRepo;
        this.jwt = jwt;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        if (req.email == null || req.password == null || req.handle == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error","invalid_request"));
        }
        if (chefRepo.findByEmail(req.email).isPresent()) return ResponseEntity.status(409).body(java.util.Map.of("error","email_exists"));
        if (chefRepo.findByHandle(req.handle).isPresent()) return ResponseEntity.status(409).body(java.util.Map.of("error","handle_exists"));
        Chef c = new Chef();
        c.setEmail(req.email);
        c.setHandle(req.handle);
        c.setDisplayName(req.displayName);
        c.setPasswordHash(BCrypt.hashpw(req.password, BCrypt.gensalt()));
        HashSet<String> roles = new HashSet<>();
        if ("chef".equalsIgnoreCase(req.role)) roles.add("CHEF"); else roles.add("USER");
        if ("admin".equalsIgnoreCase(req.role)) roles.add("ADMIN");
        c.setRoles(roles);
        c.setEmailVerified(!emailVerificationEnabled);
        chefRepo.save(c);
        return ResponseEntity.ok(java.util.Map.of("id", c.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<Chef> opt = chefRepo.findByEmail(req.email);
        if (opt.isEmpty()) return ResponseEntity.status(401).body(java.util.Map.of("error","invalid_credentials"));
        Chef c = opt.get();
        if (!BCrypt.checkpw(req.password, c.getPasswordHash())) return ResponseEntity.status(401).body(java.util.Map.of("error","invalid_credentials"));
        String access = jwt.createAccessToken(c.getId(), c.getRoles());
        String refresh = jwt.createRefreshToken(c.getId());
        // store hashed refresh
        refreshRepo.deleteByChef(c);
        RefreshToken rt = new RefreshToken();
        rt.setChef(c);
        rt.setTokenHash(org.apache.commons.codec.digest.DigestUtils.sha256Hex(refresh));
        rt.setExpiresAt(Instant.now().plusMillis(Long.parseLong(System.getProperty("jwt.refresh-ttl-ms", "604800000"))));
        refreshRepo.save(rt);
        return ResponseEntity.ok(new LoginResponse(access, refresh, Long.parseLong(System.getProperty("jwt.ttl-ms", "900000"))));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
        try {
            var jwtClaims = jwt.parseToken(req.refreshToken).getBody();
            UUID uid = UUID.fromString(jwtClaims.getSubject());
            Chef c = chefRepo.findById(uid).orElseThrow();
            var stored = refreshRepo.findByChef(c).orElseThrow();
            String hash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(req.refreshToken);
            if (!stored.getTokenHash().equals(hash)) return ResponseEntity.status(401).body(java.util.Map.of("error","invalid_refresh"));
            String access = jwt.createAccessToken(c.getId(), c.getRoles());
            String refresh = jwt.createRefreshToken(c.getId());
            // rotate
            stored.setTokenHash(org.apache.commons.codec.digest.DigestUtils.sha256Hex(refresh));
            stored.setExpiresAt(Instant.now().plusMillis(604800000L));
            refreshRepo.save(stored);
            return ResponseEntity.ok(new LoginResponse(access, refresh, Long.parseLong(System.getProperty("jwt.ttl-ms", "900000"))));
        } catch (Exception ex) {
            return ResponseEntity.status(401).body(java.util.Map.of("error","invalid_refresh"));
        }
    }
}
