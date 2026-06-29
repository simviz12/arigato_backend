package com.restaurant.domain.repository;

import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubproductRepository {
    Subproduct save(Subproduct subproduct, List<SubproductIngredient> ingredients);
    Subproduct update(Subproduct subproduct, List<SubproductIngredient> ingredients);
    Optional<Subproduct> findById(UUID id);
    List<SubproductIngredient> findIngredientsBySubproductId(UUID subproductId);
    List<Subproduct> findAll();
}
