package com.restaurant.domain.port;

import com.restaurant.domain.model.TokenPair;
import com.restaurant.domain.model.User;

public interface TokenGeneratorPort {
    TokenPair generateTokens(User user);
    String extractUsernameFromRefreshToken(String refreshToken);
    boolean validateRefreshToken(String refreshToken);
}
