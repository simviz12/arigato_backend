package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Weight;
import lombok.Value;

import java.util.UUID;

@Value
public class FinalProductComponent {
    UUID finalProductId;
    ComponentReference reference;
    Weight quantity;

    public FinalProductComponent(UUID finalProductId, ComponentReference reference, Weight quantity) {
        if (finalProductId == null) throw new IllegalArgumentException("Final product ID required");
        if (reference == null) throw new IllegalArgumentException("Component reference required");
        if (quantity == null || quantity.getGrams().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        this.finalProductId = finalProductId;
        this.reference = reference;
        this.quantity = quantity;
    }
}
