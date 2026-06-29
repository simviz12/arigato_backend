package com.restaurant.infrastructure.repository;

import com.restaurant.infrastructure.entity.PurchaseEntity;
public interface SpringDataPurchaseRepository extends org.springframework.data.jpa.repository.JpaRepository<PurchaseEntity, java.util.UUID> {
    java.util.List<PurchaseEntity> findByPrimaryProductId(java.util.UUID primaryProductId);
}
