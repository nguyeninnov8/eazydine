package com.nguyeninnov8.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public String generateToken(String username, Long userId) {
        return generateToken(username, userId, new HashMap<>());
    }

    public String generateToken(String username, Long userId, HashMap<String, Object> claims) {
        return buildToken(username, userId, claims, expiration);
    }

    private String buildToken(String username, Long userId, HashMap<String, Object> claims, long expiration) {
        claims.put("userId", userId);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Map<String, Boolean> isTokenValid(String accessToken) {
        return Map.of("valid", !isTokenExpired(accessToken));
    }

    private boolean isTokenExpired(String accessToken) {
        return extractExpiration(accessToken).before(new Date());
    }

    private Date extractExpiration(String accessToken) {
        return extractClaim(accessToken, Claims::getExpiration);
    }
}