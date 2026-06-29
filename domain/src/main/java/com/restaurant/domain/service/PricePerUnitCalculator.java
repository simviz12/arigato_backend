package com.restaurant.domain.service;

import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PricePerUnitCalculator {

    /**
     * Computes the exact cost per gram (price / quantity) retaining 6 decimal places.
     * Prevents ArithmeticException for non-terminating decimals (e.g., 10 / 3).
     */
    public static BigDecimal calculateCostPerGram(Money price, Weight quantity) {
        if (price == null || quantity == null) {
            throw new IllegalArgumentException("Price and quantity must not be null");
        }
        if (quantity.getGrams().compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot divide by zero quantity");
        }
        
        BigDecimal pricePesos = price.getPesos();
        BigDecimal quantityGrams = quantity.getGrams();
        
        return pricePesos.divide(quantityGrams, 6, RoundingMode.HALF_UP);
    }
}
