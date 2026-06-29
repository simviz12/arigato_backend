package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Weight;
import lombok.Value;

import java.util.UUID;

@Value
public class SubproductIngredient {
    UUID subproductId;
    UUID primaryProductId;
    Weight quantity;

    public SubproductIngredient(UUID subproductId, UUID primaryProductId, Weight quantity) {
        if (subproductId == null) throw new IllegalArgumentException("Subproduct ID is required");
        if (primaryProductId == null) throw new IllegalArgumentException("Primary Product ID is required");
        if (quantity == null || quantity.getGrams().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ingredient quantity must be positive");
        }
        
        this.subproductId = subproductId;
        this.primaryProductId = primaryProductId;
        this.quantity = quantity;
    }
}
