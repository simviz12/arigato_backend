package com.restaurant.domain.strategy;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;

import java.math.BigDecimal;
import java.util.List;

public interface CostingStrategy {
    /**
     * Calculates the cost per gram of a primary product based on its purchase history.
     */
    BigDecimal calculateCostPerGram(PrimaryProduct product, List<Purchase> purchaseHistory);
}
