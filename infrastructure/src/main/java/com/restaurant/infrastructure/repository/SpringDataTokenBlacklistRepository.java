package com.restaurant.infrastructure.repository;

import com.restaurant.infrastructure.entity.TokenBlacklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface SpringDataTokenBlacklistRepository extends JpaRepository<TokenBlacklistEntity, String> {
    void deleteByExpiresAtBefore(LocalDateTime now);
}
