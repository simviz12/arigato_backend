package com.restaurant.domain.strategy;

import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.service.PrimaryProductStockModifier;

import java.util.List;

public class BatchDeductionStrategy implements SubproductDeductionStrategy {

    @Override
    public void deduct(Subproduct subproduct, List<SubproductIngredient> recipe, Weight quantityNeededGrams, PrimaryProductStockModifier stockModifier) {
        // Batch mode strictly deducts from the pre-prepared batch stock.
        // It does not deduct primary products at this time, because they were 
        // already deducted when the batch was originally prepared.
        subproduct.deductBatchStock(quantityNeededGrams);
    }
}
