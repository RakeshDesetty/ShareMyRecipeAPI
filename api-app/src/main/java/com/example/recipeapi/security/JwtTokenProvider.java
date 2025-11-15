package com.example.recipeapi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private final Key key;
    private final long ttlMs;
    private final long refreshTtlMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.ttl-ms}") long ttlMs,
                            @Value("${jwt.refresh-ttl-ms}") long refreshTtlMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.ttlMs = ttlMs;
        this.refreshTtlMs = refreshTtlMs;
    }

    public String createAccessToken(UUID userId, Set<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMs);
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(UUID userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTtlMs);
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
