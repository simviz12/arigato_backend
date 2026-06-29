package com.restaurant.domain.service;

import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductCostResult;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.strategy.CostingStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SubproductCostCalculator {

    public static SubproductCostResult calculate(
            Subproduct subproduct,
            List<SubproductIngredient> recipe,
            Map<UUID, PrimaryProduct> primaryProductsMap,
            Map<UUID, List<Purchase>> purchasesMap,
            CostingStrategy costingStrategy
    ) {
        if (recipe == null || recipe.isEmpty()) {
            return new SubproductCostResult(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal totalRecipeCostPesos = BigDecimal.ZERO;

        for (SubproductIngredient ingredient : recipe) {
            UUID productId = ingredient.getPrimaryProductId();
            PrimaryProduct primaryProduct = primaryProductsMap.get(productId);
            List<Purchase> purchases = purchasesMap.get(productId);
            
            if (primaryProduct == null) {
                continue; // Missing data edge case
            }

            BigDecimal costPerGram = costingStrategy.calculateCostPerGram(primaryProduct, purchases);
            BigDecimal ingredientCost = costPerGram.multiply(ingredient.getQuantity().getGrams()).setScale(6, RoundingMode.HALF_UP);
            
            totalRecipeCostPesos = totalRecipeCostPesos.add(ingredientCost);
        }

        BigDecimal totalYieldGrams = subproduct.getTotalYield().getGrams();
        BigDecimal costPerGramPesos = BigDecimal.ZERO;

        if (totalYieldGrams.compareTo(BigDecimal.ZERO) > 0) {
            costPerGramPesos = totalRecipeCostPesos.divide(totalYieldGrams, 6, RoundingMode.HALF_UP);
        }

        return new SubproductCostResult(costPerGramPesos, totalRecipeCostPesos);
    }
}
