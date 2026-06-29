package com.restaurant.domain.strategy;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class WeightedAverageCostingStrategy implements CostingStrategy {

    @Override
    public BigDecimal calculateCostPerGram(PrimaryProduct product, List<Purchase> purchaseHistory) {
        if (purchaseHistory == null || purchaseHistory.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalCostPesos = BigDecimal.ZERO;
        BigDecimal totalQuantityGrams = BigDecimal.ZERO;

        for (Purchase purchase : purchaseHistory) {
            totalCostPesos = totalCostPesos.add(purchase.getTotalPrice().getPesos());
            totalQuantityGrams = totalQuantityGrams.add(purchase.getQuantityGrams().getGrams());
        }

        if (totalQuantityGrams.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalCostPesos.divide(totalQuantityGrams, 6, RoundingMode.HALF_UP);
    }
}
