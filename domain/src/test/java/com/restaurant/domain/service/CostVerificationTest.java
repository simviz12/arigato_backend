package com.restaurant.domain.service;

import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductCostResult;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.strategy.WeightedAverageCostingStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CostVerificationTest {

    @Test
    void verifyScenariosFromSpreadsheet() {
        // SCENARIO 2: Floating point prices
        // Yield: 1000g. Ing1: 500g @ 2.333333. Ing2: 100g @ 4.5
        // Expected Batch Cost: 1616.6665. Expected Cost Per Gram: 1.616667
        
        UUID ing1Id = UUID.randomUUID();
        UUID ing2Id = UUID.randomUUID();

        PrimaryProduct ing1 = PrimaryProduct.builder().id(ing1Id).name("Ing1").build();
        PrimaryProduct ing2 = PrimaryProduct.builder().id(ing2Id).name("Ing2").build();

        // 500g @ 2.333333 = 1166.6665. We simulate this by having 1000g cost 2333.333
        Purchase p1 = Purchase.builder().primaryProductId(ing1Id).quantity(Weight.ofGrams(1000)).totalPrice(Money.ofPesos(2333.333)).build();
        // 100g @ 4.5 = 450. We simulate this by having 100g cost 450
        Purchase p2 = Purchase.builder().primaryProductId(ing2Id).quantity(Weight.ofGrams(100)).totalPrice(Money.ofPesos(450)).build();

        Subproduct sauce = Subproduct.builder()
                .name("Sauce")
                .totalYield(Weight.ofGrams(1000))
                .preparationMode(PreparationMode.BATCH)
                .build();

        List<SubproductIngredient> recipe = List.of(
                new SubproductIngredient(sauce.getId(), ing1Id, Weight.ofGrams(500)),
                new SubproductIngredient(sauce.getId(), ing2Id, Weight.ofGrams(100))
        );

        Map<UUID, PrimaryProduct> products = Map.of(ing1Id, ing1, ing2Id, ing2);
        Map<UUID, List<Purchase>> purchases = Map.of(ing1Id, List.of(p1), ing2Id, List.of(p2));

        SubproductCostResult result = SubproductCostCalculator.calculate(
                sauce, recipe, products, purchases, new WeightedAverageCostingStrategy()
        );

        assertThat(result.getTotalBatchCostPesos()).isEqualByComparingTo(new BigDecimal("1616.666500"));
        assertThat(result.getCostPerGramPesos()).isEqualByComparingTo(new BigDecimal("1.616667"));
    }
}
