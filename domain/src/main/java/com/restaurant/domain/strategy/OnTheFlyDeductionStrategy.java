package com.restaurant.domain.strategy;

import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.service.PrimaryProductStockModifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OnTheFlyDeductionStrategy implements SubproductDeductionStrategy {

    @Override
    public void deduct(Subproduct subproduct, List<SubproductIngredient> recipe, Weight quantityNeededGrams, PrimaryProductStockModifier stockModifier) {
        if (recipe == null || recipe.isEmpty()) {
            throw new IllegalStateException("Cannot prepare ON_THE_FLY subproduct without a recipe");
        }

        BigDecimal neededGrams = quantityNeededGrams.getGrams();
        BigDecimal totalYield = subproduct.getTotalYield().getGrams();

        // Calculate the ratio: quantityNeeded / totalYield
        // E.g., Need 500g of a 1000g recipe = 0.5 ratio
        BigDecimal ratio = neededGrams.divide(totalYield, 6, RoundingMode.HALF_UP);

        for (SubproductIngredient ingredient : recipe) {
            BigDecimal rawIngredientGrams = ingredient.getQuantity().getGrams();
            BigDecimal requiredGrams = rawIngredientGrams.multiply(ratio).setScale(6, RoundingMode.HALF_UP);
            
            // Deduct the exact proportional amount of the raw primary product
            stockModifier.deductPrimaryProductStock(ingredient.getPrimaryProductId(), Weight.ofGrams(requiredGrams));
        }
    }
}
