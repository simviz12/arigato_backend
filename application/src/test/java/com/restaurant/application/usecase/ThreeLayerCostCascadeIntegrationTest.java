package com.restaurant.application.usecase;

import com.restaurant.domain.model.ComponentReference;
import com.restaurant.domain.model.FinalProductComponent;
import com.restaurant.domain.model.PreparationMode;
import com.restaurant.domain.model.PrimaryComponentRef;
import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductComponentRef;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.UnitOfMeasure;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.PrimaryProductRepository;
import com.restaurant.domain.repository.PurchaseRepository;
import com.restaurant.domain.repository.SubproductRepository;
import com.restaurant.domain.service.FinalProductCostCalculator;
import com.restaurant.domain.strategy.WeightedAverageCostingStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ThreeLayerCostCascadeIntegrationTest {

    @Test
    void verifiesExactMonetarySumAcrossAllLayers() {
        // LAYER 1: Raw Materials (Primary Products)
        UUID papaId = UUID.randomUUID();
        PrimaryProduct papa = PrimaryProduct.builder().id(papaId).name("Papa").unitOfMeasure(UnitOfMeasure.GRAM).build();
        // Bought 10,000g of Papa for $20,000 -> Cost = $2/gram
        Purchase papaPurchase = Purchase.builder().primaryProductId(papaId).distributorId(UUID.randomUUID())
                .quantity(Weight.ofGrams(10000)).totalPrice(Money.ofPesos(20000)).build();

        UUID tomateId = UUID.randomUUID();
        PrimaryProduct tomate = PrimaryProduct.builder().id(tomateId).name("Tomate").unitOfMeasure(UnitOfMeasure.GRAM).build();
        // Bought 5,000g of Tomate for $15,000 -> Cost = $3/gram
        Purchase tomatePurchase = Purchase.builder().primaryProductId(tomateId).distributorId(UUID.randomUUID())
                .quantity(Weight.ofGrams(5000)).totalPrice(Money.ofPesos(15000)).build();

        // LAYER 2: Subproduct (Salsa BBQ)
        // Recipe: yields 1000g. Uses 800g of Tomate.
        // Cost of BBQ = (800 * 3) / 1000 = $2.4/gram
        UUID bbqId = UUID.randomUUID();
        Subproduct salsaBbq = Subproduct.builder().id(bbqId).name("Salsa BBQ").totalYield(Weight.ofGrams(1000)).preparationMode(PreparationMode.BATCH).build();
        List<SubproductIngredient> bbqRecipe = List.of(new SubproductIngredient(bbqId, tomateId, Weight.ofGrams(800)));

        // LAYER 3: Final Product (Papas BBQ)
        // Components: 200g of Papa (Primary) + 50g of Salsa BBQ (Subproduct)
        // Expected Cost: (200 * 2) + (50 * 2.4) = 400 + 120 = $520
        List<FinalProductComponent> finalComponents = List.of(
                new FinalProductComponent(UUID.randomUUID(), new PrimaryComponentRef(papaId), Weight.ofGrams(200)),
                new FinalProductComponent(UUID.randomUUID(), new SubproductComponentRef(bbqId), Weight.ofGrams(50))
        );

        // ACT
        Money totalCost = FinalProductCostCalculator.calculate(
                finalComponents,
                Map.of(papaId, papa, tomateId, tomate),
                Map.of(papaId, List.of(papaPurchase), tomateId, List.of(tomatePurchase)),
                Map.of(bbqId, salsaBbq),
                Map.of(bbqId, bbqRecipe),
                new WeightedAverageCostingStrategy()
        );

        // ASSERT exact match to the cent
        assertThat(totalCost.getPesos()).isEqualByComparingTo(new BigDecimal("520"));
    }
}
