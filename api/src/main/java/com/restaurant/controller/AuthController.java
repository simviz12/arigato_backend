package com.restaurant.controller;

import com.restaurant.application.usecase.AuthUseCase;
import com.restaurant.domain.model.TokenPair;
import com.restaurant.dto.LoginRequest;
import com.restaurant.dto.RefreshTokenRequest;
import com.restaurant.dto.TokenResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final ConcurrentHashMap<String, Bucket> loginBuckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String ip) {
        return loginBuckets.computeIfAbsent(ip, k -> {
            Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        });
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        String ip = httpRequest.getRemoteAddr();
        Bucket bucket = resolveBucket(ip);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(429).build(); // Too Many Requests
        }

        TokenPair pair = authUseCase.login(request.getUsername(), request.getPassword());
        
        Cookie refreshTokenCookie = new Cookie("refreshToken", pair.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Should be true in prod, fine for dev if localhost
        refreshTokenCookie.setPath("/api/auth");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(new TokenResponse(pair.getAccessToken(), null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        TokenPair pair = authUseCase.refreshToken(refreshToken);
        
        Cookie refreshTokenCookie = new Cookie("refreshToken", pair.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/api/auth");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(new TokenResponse(pair.getAccessToken(), null));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authUseCase.logout(refreshToken);
        }
        
        Cookie clearCookie = new Cookie("refreshToken", null);
        clearCookie.setHttpOnly(true);
        clearCookie.setSecure(true);
        clearCookie.setPath("/api/auth");
        clearCookie.setMaxAge(0);
        response.addCookie(clearCookie);

        return ResponseEntity.noContent().build();
    }
}
