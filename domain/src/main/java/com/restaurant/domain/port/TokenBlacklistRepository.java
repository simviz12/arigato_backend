package com.restaurant.domain.port;

import java.time.LocalDateTime;

public interface TokenBlacklistRepository {
    void blacklistToken(String token, LocalDateTime expiresAt);
    boolean isBlacklisted(String token);
}
