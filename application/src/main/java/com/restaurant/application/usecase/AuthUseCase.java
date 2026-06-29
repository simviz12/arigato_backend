package com.restaurant.application.usecase;

import com.restaurant.domain.model.TokenPair;

public interface AuthUseCase {
    TokenPair login(String username, String rawPassword);
    TokenPair refreshToken(String refreshToken);
    void logout(String refreshToken);
}
