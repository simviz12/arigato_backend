package com.restaurant.infrastructure.repository;

import com.restaurant.infrastructure.entity.FinalProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataFinalProductRepository extends JpaRepository<FinalProductEntity, UUID> {
}
