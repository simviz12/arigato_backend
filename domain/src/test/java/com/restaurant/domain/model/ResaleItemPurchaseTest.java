package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResaleItemPurchaseTest {

    @Test
    void purchasingResaleItemComputesPerUnitCostCorrectly() {
        UUID cokeId = UUID.randomUUID();

        // 12 x Coca-Cola purchased for $24,000 Pesos
        Purchase purchase = Purchase.builder()
                .primaryProductId(cokeId)
                .distributorId(UUID.randomUUID())
                .quantity(Weight.ofGrams(12)) // 'Grams' wrapper acts as abstract 'Quantity'
                .totalPrice(Money.ofPesos(24000))
                .build();

        // 24000 / 12 = 2000 per unit
        assertThat(purchase.getCostPerUnitOrGram().getPesos()).isEqualByComparingTo(new BigDecimal("2000"));
    }

    @Test
    void deductsUnitStockCorrectly() {
        PrimaryProduct coke = PrimaryProduct.builder()
                .name("Coca-Cola")
                .unitOfMeasure(UnitOfMeasure.UNIT)
                .currentStock(Weight.ofGrams(12)) // 12 units
                .build();

        // Sell 3 Cokes
        coke.deduct(new BigDecimal("3"));

        assertThat(coke.getCurrentStock().getGrams()).isEqualByComparingTo(new BigDecimal("9"));
    }
}
