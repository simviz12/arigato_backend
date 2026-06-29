package com.restaurant.domain.service;

import com.restaurant.domain.model.ComponentReference;
import com.restaurant.domain.model.FinalProductComponent;
import com.restaurant.domain.model.PrimaryComponentRef;
import com.restaurant.domain.model.PrimaryProduct;
import com.restaurant.domain.model.Purchase;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductComponentRef;
import com.restaurant.domain.model.SubproductCostResult;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.strategy.CostingStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FinalProductCostCalculator {

    public static Money calculate(
            List<FinalProductComponent> components,
            Map<UUID, PrimaryProduct> primaryProductsMap,
            Map<UUID, List<Purchase>> purchasesMap,
            Map<UUID, Subproduct> subproductsMap,
            Map<UUID, List<SubproductIngredient>> subproductRecipesMap,
            CostingStrategy costingStrategy
    ) {
        if (components == null || components.isEmpty()) {
            return Money.ofPesos(0);
        }

        BigDecimal totalCostPesos = BigDecimal.ZERO;

        for (FinalProductComponent component : components) {
            ComponentReference ref = component.getReference();
            BigDecimal quantityGrams = component.getQuantity().getGrams();
            
            if (ref instanceof PrimaryComponentRef primaryRef) {
                UUID pId = primaryRef.getPrimaryProductId();
                PrimaryProduct primaryProduct = primaryProductsMap.get(pId);
                List<Purchase> purchases = purchasesMap.get(pId);
                
                if (primaryProduct != null) {
                    BigDecimal costPerGram = costingStrategy.calculateCostPerGram(primaryProduct, purchases);
                    BigDecimal componentCost = costPerGram.multiply(quantityGrams).setScale(6, RoundingMode.HALF_UP);
                    totalCostPesos = totalCostPesos.add(componentCost);
                }
            } else if (ref instanceof SubproductComponentRef subproductRef) {
                UUID sId = subproductRef.getSubproductId();
                Subproduct subproduct = subproductsMap.get(sId);
                List<SubproductIngredient> recipe = subproductRecipesMap.get(sId);
                
                if (subproduct != null && recipe != null) {
                    // Recursively calculate subproduct cost
                    SubproductCostResult subResult = SubproductCostCalculator.calculate(
                            subproduct, recipe, primaryProductsMap, purchasesMap, costingStrategy
                    );
                    
                    BigDecimal subproductCostPerGram = subResult.getCostPerGramPesos();
                    BigDecimal componentCost = subproductCostPerGram.multiply(quantityGrams).setScale(6, RoundingMode.HALF_UP);
                    totalCostPesos = totalCostPesos.add(componentCost);
                }
            }
        }

        return Money.ofPesos(totalCostPesos.setScale(0, RoundingMode.HALF_UP).doubleValue());
    }
}
