package com.restaurant.infrastructure.security;

import com.restaurant.domain.model.TokenPair;
import com.restaurant.domain.model.User;
import com.restaurant.domain.port.TokenGeneratorPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider implements TokenGeneratorPort {

    private final SecretKey key;
    private final long accessTokenValidityMs = 15 * 60 * 1000; // 15 minutes
    private final long refreshTokenValidityMs = 7L * 24 * 60 * 60 * 1000; // 7 days

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public TokenPair generateTokens(User user) {
        Date now = new Date();
        Date accessExpiration = new Date(now.getTime() + accessTokenValidityMs);
        Date refreshExpiration = new Date(now.getTime() + refreshTokenValidityMs);

        String accessToken = Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(accessExpiration)
                .signWith(key)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(user.getUsername())
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(refreshExpiration)
                .signWith(key)
                .compact();

        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public String extractUsernameFromRefreshToken(String refreshToken) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();
        
        if (!"REFRESH".equals(claims.get("type"))) {
            throw new RuntimeException("Not a valid refresh token");
        }
        
        return claims.getSubject();
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(refreshToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
