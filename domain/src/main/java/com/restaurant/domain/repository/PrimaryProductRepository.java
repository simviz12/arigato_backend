package com.restaurant.domain.repository;

import com.restaurant.domain.model.PrimaryProduct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrimaryProductRepository {
    PrimaryProduct save(PrimaryProduct product);
    Optional<PrimaryProduct> findById(UUID id);
    List<PrimaryProduct> findAll();
}
