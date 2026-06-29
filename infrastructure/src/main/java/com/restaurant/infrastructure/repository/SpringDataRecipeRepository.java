package com.restaurant.infrastructure.repository;

import com.restaurant.infrastructure.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataRecipeRepository extends JpaRepository<RecipeEntity, UUID> {
    List<RecipeEntity> findBySubproductId(UUID subproductId);
}
