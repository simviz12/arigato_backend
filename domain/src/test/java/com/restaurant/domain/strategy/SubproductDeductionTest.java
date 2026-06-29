package com.restaurant.domain.strategy;

import com.restaurant.domain.exception.InsufficientBatchStockException;
import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.service.PrimaryProductStockModifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubproductDeductionTest {

    private UUID subproductId;
    private UUID tomatoId;
    private UUID onionId;

    private List<SubproductIngredient> recipe;

    @BeforeEach
    void setUp() {
        subproductId = UUID.randomUUID();
        tomatoId = UUID.randomUUID();
        onionId = UUID.randomUUID();

        // Recipe for 1000g of House Sauce
        recipe = List.of(
                new SubproductIngredient(subproductId, tomatoId, Weight.ofGrams(800)),
                new SubproductIngredient(subproductId, onionId, Weight.ofGrams(200))
        );
    }

    @Test
    void batchStrategy_throwsExceptionWhenStockIsInsufficient() {
        Subproduct batchSauce = Subproduct.builder()
                .id(subproductId)
                .name("Batch Sauce")
                .totalYield(Weight.ofGrams(1000))
                .preparationMode(PreparationMode.BATCH)
                .currentBatchStock(Weight.ofGrams(200)) // Only 200g available
                .build();

        SubproductDeductionStrategy strategy = SubproductDeductionStrategyFactory.getStrategy(PreparationMode.BATCH);
        
        // Mock stock modifier (should never be called for Batch deduction)
        PrimaryProductStockModifier modifier = (id, quantity) -> {
            throw new RuntimeException("Should not deduct raw materials when deducting from batch!");
        };

        // Try to deduct 500g when only 200g available
        assertThatThrownBy(() -> strategy.deduct(batchSauce, recipe, Weight.ofGrams(500), modifier))
                .isInstanceOf(InsufficientBatchStockException.class)
                .hasMessageContaining("Not enough batch stock");
    }

    @Test
    void strategies_proveMathematicalEquivalenceForRawMaterials() {
        // --- Scenario 1: ON THE FLY Deduction ---
        Subproduct otfSauce = Subproduct.builder()
                .id(subproductId)
                .name("OTF Sauce")
                .totalYield(Weight.ofGrams(1000)) // Base recipe yield
                .preparationMode(PreparationMode.ON_THE_FLY)
                .build();

        Map<UUID, BigDecimal> otfConsumption = new HashMap<>();
        PrimaryProductStockModifier otfModifier = (id, quantity) -> {
            otfConsumption.merge(id, quantity.getGrams(), BigDecimal::add);
        };

        SubproductDeductionStrategy otfStrategy = SubproductDeductionStrategyFactory.getStrategy(PreparationMode.ON_THE_FLY);
        // Customer orders 500g of sauce
        otfStrategy.deduct(otfSauce, recipe, Weight.ofGrams(500), otfModifier);

        // --- Scenario 2: BATCH Deduction ---
        // First we "prepare" 1000g (consuming raw materials exactly as OTF would for 1000g)
        Map<UUID, BigDecimal> batchConsumption = new HashMap<>();
        PrimaryProductStockModifier batchPrepModifier = (id, quantity) -> {
            batchConsumption.merge(id, quantity.getGrams(), BigDecimal::add);
        };
        // Preparing a batch is mathematically identical to an on-the-fly request of the full yield
        otfStrategy.deduct(otfSauce, recipe, Weight.ofGrams(1000), batchPrepModifier); 

        // Then we deduct 500g from the prepared batch stock
        Subproduct batchSauce = Subproduct.builder()
                .id(subproductId)
                .name("Batch Sauce")
                .totalYield(Weight.ofGrams(1000))
                .preparationMode(PreparationMode.BATCH)
                .currentBatchStock(Weight.ofGrams(1000)) // Now we have 1000g
                .build();
        
        SubproductDeductionStrategy batchStrategy = SubproductDeductionStrategyFactory.getStrategy(PreparationMode.BATCH);
        batchStrategy.deduct(batchSauce, recipe, Weight.ofGrams(500), batchPrepModifier);
        
        // Assert OTF consumed exactly half the recipe
        assertThat(otfConsumption.get(tomatoId)).isEqualByComparingTo("400");
        assertThat(otfConsumption.get(onionId)).isEqualByComparingTo("100");

        // Assert Batch consumed the FULL recipe (since we prepared 1000g regardless of only selling 500g)
        assertThat(batchConsumption.get(tomatoId)).isEqualByComparingTo("800");
        assertThat(batchConsumption.get(onionId)).isEqualByComparingTo("200");
        
        // Assert Batch remaining stock is exactly 500g
        assertThat(batchSauce.getCurrentBatchStock().getGrams()).isEqualByComparingTo("500");
    }
}
