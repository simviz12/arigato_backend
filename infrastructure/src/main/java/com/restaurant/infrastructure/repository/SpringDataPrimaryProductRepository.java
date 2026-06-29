package com.restaurant.infrastructure.repository;

import com.restaurant.infrastructure.entity.PrimaryProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPrimaryProductRepository extends JpaRepository<PrimaryProductEntity, UUID> {
    Optional<PrimaryProductEntity> findByName(String name);
}
