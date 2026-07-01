package com.restaurant.infrastructure.repository;

import com.restaurant.infrastructure.entity.DistributorOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataDistributorOfferRepository extends JpaRepository<DistributorOfferEntity, UUID> {
    List<DistributorOfferEntity> findByPrimaryProductId(UUID primaryProductId);
    void deleteByDistributorIdAndPrimaryProductId(UUID distributorId, UUID primaryProductId);
}
