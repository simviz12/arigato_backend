package com.restaurant.domain.service;

import com.restaurant.domain.model.ComponentReference;
import com.restaurant.domain.model.FinalProductComponent;
import com.restaurant.domain.model.PrimaryComponentRef;
import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductComponentRef;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.repository.SubproductRepository;
import com.restaurant.domain.strategy.SubproductDeductionStrategy;
import com.restaurant.domain.strategy.SubproductDeductionStrategyFactory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class FinalProductDeductionService {

    private final PrimaryProductStockModifier primaryStockModifier;
    private final SubproductRepository subproductRepository;

    /**
     * Given a sale of N units of a Final Product, this service iterates through its components
     * and applies the correct deduction logic (direct for primary, via strategy for subproducts).
     */
    public void deductForSale(List<FinalProductComponent> components, int unitsSold) {
        if (unitsSold <= 0) return;
        
        BigDecimal multiplier = BigDecimal.valueOf(unitsSold);

        for (FinalProductComponent component : components) {
            ComponentReference ref = component.getReference();
            Weight totalRequiredQuantity = Weight.ofGrams(
                    component.getQuantity().getGrams().multiply(multiplier)
            );

            if (ref instanceof PrimaryComponentRef primaryRef) {
                // Direct deduction of raw material
                primaryStockModifier.deductPrimaryProductStock(primaryRef.getPrimaryProductId(), totalRequiredQuantity);
            } 
            else if (ref instanceof SubproductComponentRef subproductRef) {
                // Delegate to subproduct strategy
                UUID sId = subproductRef.getSubproductId();
                Subproduct subproduct = subproductRepository.findById(sId)
                        .orElseThrow(() -> new IllegalStateException("Subproduct not found: " + sId));
                
                List<SubproductIngredient> recipe = subproductRepository.findIngredientsBySubproductId(sId);
                
                SubproductDeductionStrategy strategy = SubproductDeductionStrategyFactory.getStrategy(subproduct.getPreparationMode());
                
                // The strategy handles whether it deducts from Batch Stock or On-The-Fly primary products
                strategy.deduct(subproduct, recipe, totalRequiredQuantity, primaryStockModifier);
                
                // If it was a BATCH deduction, we must persist the updated batch stock state
                if (subproduct.getPreparationMode() == com.restaurant.domain.model.PreparationMode.BATCH) {
                    subproductRepository.update(subproduct, recipe);
                }
            }
        }
    }
}
