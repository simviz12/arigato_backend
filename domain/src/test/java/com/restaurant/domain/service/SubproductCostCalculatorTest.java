package com.restaurant.domain.service;

import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductCostResult;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.strategy.CostingStrategy;
import com.restaurant.domain.strategy.WeightedAverageCostingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SubproductCostCalculatorTest {

    private UUID tomatoId = UUID.randomUUID();
    private UUID onionId = UUID.randomUUID();
    
    private PrimaryProduct tomato;
    private PrimaryProduct onion;

    @BeforeEach
    void setUp() {
        tomato = PrimaryProduct.builder().id(tomatoId).name("Tomato").build();
        onion = PrimaryProduct.builder().id(onionId).name("Onion").build();
    }

    @Test
    void testWeightedAverageCost_WithFluctuatingPrices() {
        // SCENARIO 1: We bought tomatoes twice at different prices.
        // Purchase 1: 1000g for $2000 (2 pesos/g)
        Purchase p1 = Purchase.builder().primaryProductId(tomatoId).quantity(Weight.ofGrams(1000)).totalPrice(Money.ofPesos(2000)).build();
        // Purchase 2: 500g for $1500 (3 pesos/g)
        Purchase p2 = Purchase.builder().primaryProductId(tomatoId).quantity(Weight.ofGrams(500)).totalPrice(Money.ofPesos(1500)).build();
        
        // Total = 1500g for $3500. Weighted average cost = 3500/1500 = 2.333333 pesos/g

        // We bought onions once. 200g for $800 (4 pesos/g)
        Purchase p3 = Purchase.builder().primaryProductId(onionId).quantity(Weight.ofGrams(200)).totalPrice(Money.ofPesos(800)).build();

        Subproduct sauce = Subproduct.builder()
                .name("Sauce")
                .totalYield(Weight.ofGrams(1000))
                .preparationMode(PreparationMode.BATCH)
                .build();

        // Recipe needs 500g Tomato, 100g Onion
        List<SubproductIngredient> recipe = List.of(
                new SubproductIngredient(sauce.getId(), tomatoId, Weight.ofGrams(500)),
                new SubproductIngredient(sauce.getId(), onionId, Weight.ofGrams(100))
        );

        Map<UUID, PrimaryProduct> products = Map.of(tomatoId, tomato, onionId, onion);
        Map<UUID, List<Purchase>> purchases = Map.of(tomatoId, List.of(p1, p2), onionId, List.of(p3));

        SubproductCostResult result = SubproductCostCalculator.calculate(
                sauce, recipe, products, purchases, new WeightedAverageCostingStrategy()
        );

        // Tomato cost: 500g * 2.333333 = 1166.666500
        // Onion cost: 100g * 4.0 = 400.000000
        // Total Batch Cost = 1566.666500 pesos
        assertThat(result.getTotalBatchCostPesos()).isEqualByComparingTo(new BigDecimal("1566.666500"));

        // Cost per gram = 1566.666500 / 1000 = 1.566667 pesos/g
        assertThat(result.getCostPerGramPesos()).isEqualByComparingTo(new BigDecimal("1.566667"));
    }

    @Test
    void testCostingStrategyOpenClosedPrinciple() {
        // Dummy Strategy: Always assumes everything costs 10 pesos/gram
        CostingStrategy dummyStrategy = (product, history) -> new BigDecimal("10.0");

        Subproduct sauce = Subproduct.builder()
                .name("Sauce")
                .totalYield(Weight.ofGrams(1000))
                .preparationMode(PreparationMode.BATCH)
                .build();

        List<SubproductIngredient> recipe = List.of(
                new SubproductIngredient(sauce.getId(), tomatoId, Weight.ofGrams(500))
        );

        Map<UUID, PrimaryProduct> products = Map.of(tomatoId, tomato);
        Map<UUID, List<Purchase>> purchases = Map.of(tomatoId, List.of()); // No purchases needed for dummy

        SubproductCostResult result = SubproductCostCalculator.calculate(
                sauce, recipe, products, purchases, dummyStrategy
        );

        // 500g * 10 pesos = 5000 total batch cost
        assertThat(result.getTotalBatchCostPesos()).isEqualByComparingTo(new BigDecimal("5000"));
    }

    @Test
    void testPerformance_20IngredientsUnder50ms() {
        Subproduct complexMeal = Subproduct.builder()
                .name("Complex")
                .totalYield(Weight.ofGrams(5000))
                .preparationMode(PreparationMode.BATCH)
                .build();

        List<SubproductIngredient> recipe = new ArrayList<>();
        Map<UUID, PrimaryProduct> products = new HashMap<>();
        Map<UUID, List<Purchase>> purchases = new HashMap<>();

        for (int i = 0; i < 20; i++) {
            UUID id = UUID.randomUUID();
            PrimaryProduct p = PrimaryProduct.builder().id(id).name("Ingredient " + i).build();
            products.put(id, p);
            
            // Give each ingredient 10 purchase history records
            List<Purchase> history = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                history.add(Purchase.builder().primaryProductId(id).quantity(Weight.ofGrams(1000)).totalPrice(Money.ofPesos(5000)).build());
            }
            purchases.put(id, history);

            recipe.add(new SubproductIngredient(complexMeal.getId(), id, Weight.ofGrams(100)));
        }

        long startTime = System.nanoTime();
        
        SubproductCostResult result = SubproductCostCalculator.calculate(
                complexMeal, recipe, products, purchases, new WeightedAverageCostingStrategy()
        );

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1000000;

        assertThat(durationMs).isLessThan(50);
        assertThat(result.getTotalBatchCostPesos()).isGreaterThan(BigDecimal.ZERO);
    }
}
