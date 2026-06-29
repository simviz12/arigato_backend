package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Money;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class FinalProduct {
    private final UUID id;
    private final String name;
    private final Money sellingPrice;
    private final String category;
    private boolean active;

    @Builder
    public FinalProduct(UUID id, String name, Money sellingPrice, String category, Boolean active) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        if (sellingPrice == null || sellingPrice.getPesos().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Selling price cannot be negative");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.sellingPrice = sellingPrice;
        this.category = category != null ? category : "GENERAL";
        this.active = active != null ? active : true;
    }

    public void deactivate() {
        this.active = false;
    }
}
