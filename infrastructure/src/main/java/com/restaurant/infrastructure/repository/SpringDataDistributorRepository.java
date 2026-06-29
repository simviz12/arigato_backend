package com.restaurant.infrastructure.repository;

import com.restaurant.infrastructure.entity.DistributorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataDistributorRepository extends JpaRepository<DistributorEntity, UUID> {
}
