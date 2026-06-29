package com.restaurant.infrastructure.repository;

import com.restaurant.domain.port.TokenBlacklistRepository;
import com.restaurant.infrastructure.entity.TokenBlacklistEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class TokenBlacklistRepositoryAdapter implements TokenBlacklistRepository {

    private final SpringDataTokenBlacklistRepository repository;

    @Override
    public void blacklistToken(String token, LocalDateTime expiresAt) {
        TokenBlacklistEntity entity = TokenBlacklistEntity.builder()
                .token(token)
                .blacklistedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .build();
        repository.save(entity);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return repository.existsById(token);
    }
}
