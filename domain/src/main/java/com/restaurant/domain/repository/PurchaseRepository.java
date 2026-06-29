package com.restaurant.domain.repository;

import com.restaurant.domain.model.Purchase;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository {
    Purchase save(Purchase purchase);
    Optional<Purchase> findById(UUID id);
    java.util.List<Purchase> findByPrimaryProductId(UUID primaryProductId);
}
