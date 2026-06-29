package com.restaurant.infrastructure.repository;

import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.ComponentReference;
import com.restaurant.domain.model.PrimaryComponentRef;
import com.restaurant.domain.model.SubproductComponentRef;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.SubproductRepository;
import com.restaurant.infrastructure.entity.RecipeEntity;
import com.restaurant.infrastructure.entity.SubproductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SubproductRepositoryAdapter implements SubproductRepository {

    private final SpringDataSubproductRepository subproductRepo;
    private final SpringDataRecipeRepository recipeRepo;

    @Override
    @Transactional
    public Subproduct save(Subproduct subproduct, List<SubproductIngredient> ingredients) {
        SubproductEntity entity = new SubproductEntity();
        entity.setId(subproduct.getId());
        entity.setName(subproduct.getName());
        entity.setTotalYieldGrams(subproduct.getTotalYield().getGrams());
        entity.setPreparationMode(subproduct.getPreparationMode());
        if (subproduct.getCurrentBatchStock() != null) {
            entity.setCurrentBatchStockGrams(subproduct.getCurrentBatchStock().getGrams());
        }
        subproductRepo.save(entity);
        saveIngredients(subproduct.getId(), ingredients);
        return subproduct;
    }

    @Override
    @Transactional
    public Subproduct update(Subproduct subproduct, List<SubproductIngredient> ingredients) {
        return save(subproduct, ingredients); // simple implementation
    }

    @Override
    public Optional<Subproduct> findById(UUID id) {
        return subproductRepo.findById(id).map(this::mapToDomain);
    }

    @Override
    public List<SubproductIngredient> findIngredientsBySubproductId(UUID subproductId) {
        return recipeRepo.findBySubproductId(subproductId).stream()
                .map(this::mapToIngredientDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Subproduct> findAll() {
        return subproductRepo.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private void saveIngredients(UUID subproductId, List<SubproductIngredient> ingredients) {
        List<RecipeEntity> oldRecipes = recipeRepo.findBySubproductId(subproductId);
        recipeRepo.deleteAll(oldRecipes);
        
        List<RecipeEntity> newRecipes = ingredients.stream().map(ing -> {
            RecipeEntity recipe = new RecipeEntity();
            recipe.setId(UUID.randomUUID());
            recipe.setSubproductId(subproductId);
            recipe.setQuantityGrams(ing.getQuantity().getGrams());
            recipe.setIngredientType("PRIMARY");
            recipe.setIngredientId(ing.getPrimaryProductId());
            return recipe;
        }).collect(Collectors.toList());
        recipeRepo.saveAll(newRecipes);
    }

    private Subproduct mapToDomain(SubproductEntity entity) {
        Weight currentBatchStock = null;
        if (entity.getPreparationMode() == PreparationMode.BATCH && entity.getCurrentBatchStockGrams() != null) {
            currentBatchStock = Weight.ofGrams(entity.getCurrentBatchStockGrams());
        }
        return Subproduct.builder()
                .id(entity.getId())
                .name(entity.getName())
                .totalYield(Weight.ofGrams(entity.getTotalYieldGrams()))
                .preparationMode(entity.getPreparationMode())
                .currentBatchStock(currentBatchStock)
                .build();
    }

    private SubproductIngredient mapToIngredientDomain(RecipeEntity entity) {
        return new SubproductIngredient(
            entity.getSubproductId(),
            entity.getIngredientId(),
            Weight.ofGrams(entity.getQuantityGrams())
        );
    }
}
