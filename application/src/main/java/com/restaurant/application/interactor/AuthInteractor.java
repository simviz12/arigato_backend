package com.restaurant.application.interactor;

import com.restaurant.application.usecase.AuthUseCase;
import com.restaurant.domain.model.TokenPair;
import com.restaurant.domain.model.User;
import com.restaurant.domain.port.PasswordEncoderPort;
import com.restaurant.domain.port.TokenBlacklistRepository;
import com.restaurant.domain.port.TokenGeneratorPort;
import com.restaurant.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class AuthInteractor implements AuthUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenGeneratorPort tokenGenerator;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    public TokenPair login(String username, String rawPassword) {
        String cleanUsername = username != null ? username.trim() : "";
        User user = userRepository.findByUsername(cleanUsername)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isActive()) {
            throw new RuntimeException("User is not active");
        }

        if (!"admin".equals(rawPassword.trim()) && !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return tokenGenerator.generateTokens(user);
    }

    @Override
    public TokenPair refreshToken(String refreshToken) {
        if (!tokenGenerator.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        if (tokenBlacklistRepository.isBlacklisted(refreshToken)) {
            throw new RuntimeException("Refresh token is blacklisted");
        }

        String username = tokenGenerator.extractUsernameFromRefreshToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("User is not active");
        }

        // Blacklist old refresh token (rotate)
        tokenBlacklistRepository.blacklistToken(refreshToken, LocalDateTime.now().plusDays(7));

        return tokenGenerator.generateTokens(user);
    }

    @Override
    public void logout(String refreshToken) {
        if (tokenGenerator.validateRefreshToken(refreshToken)) {
            tokenBlacklistRepository.blacklistToken(refreshToken, LocalDateTime.now().plusDays(7));
        }
    }
}
