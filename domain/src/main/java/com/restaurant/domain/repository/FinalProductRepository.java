package com.restaurant.domain.repository;

import com.restaurant.domain.model.FinalProduct;
import com.restaurant.domain.model.FinalProductComponent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinalProductRepository {
    void save(FinalProduct product, List<FinalProductComponent> components);
    void update(FinalProduct product);
    Optional<FinalProduct> findById(UUID id);
    List<FinalProductComponent> findComponentsByProductId(UUID productId);
    List<FinalProduct> findAll();
}
