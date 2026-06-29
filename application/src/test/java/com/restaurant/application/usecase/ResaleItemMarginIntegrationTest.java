package com.restaurant.application.usecase;

import com.restaurant.domain.model.FinalProductComponent;
import com.restaurant.domain.model.PrimaryComponentRef;
import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.UnitOfMeasure;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.service.FinalProductCostCalculator;
import com.restaurant.domain.strategy.WeightedAverageCostingStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResaleItemMarginIntegrationTest {

    @Test
    void verifiesMarginForResaleItems() {
        // SCENARIO: Coca-Cola is bought as a Resale Item (UNIT) and sold directly as a Final Product
        
        UUID cokeId = UUID.randomUUID();
        PrimaryProduct coke = PrimaryProduct.builder().id(cokeId).name("Coca-Cola").unitOfMeasure(UnitOfMeasure.UNIT).build();
        
        // Bought 12 units for $24,000 -> Cost = $2,000 per unit
        Purchase purchase = Purchase.builder()
                .primaryProductId(cokeId)
                .distributorId(UUID.randomUUID())
                .quantity(Weight.ofGrams(12)) // Abstract quantity
                .totalPrice(Money.ofPesos(24000))
                .build();
                
        // Final Product recipe is just 1 unit of Coca-Cola
        List<FinalProductComponent> finalComponents = List.of(
                new FinalProductComponent(UUID.randomUUID(), new PrimaryComponentRef(cokeId), Weight.ofGrams(1))
        );

        Money totalCost = FinalProductCostCalculator.calculate(
                finalComponents,
                Map.of(cokeId, coke),
                Map.of(cokeId, List.of(purchase)),
                Map.of(),
                Map.of(),
                new WeightedAverageCostingStrategy()
        );

        // Expected Cost = 1 unit * $2,000 = $2,000
        assertThat(totalCost.getPesos()).isEqualByComparingTo(new BigDecimal("2000"));
    }
}
