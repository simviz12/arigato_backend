package com.restaurant.domain.strategy;

import com.restaurant.domain.model.Subproduct;
import com.restaurant.domain.model.SubproductIngredient;
import com.restaurant.domain.model.vo.Weight;
import com.restaurant.domain.service.PrimaryProductStockModifier;

import java.util.List;

public interface SubproductDeductionStrategy {
    void deduct(
            Subproduct subproduct, 
            List<SubproductIngredient> recipe, 
            Weight quantityNeededGrams,
            PrimaryProductStockModifier stockModifier
    );
}
