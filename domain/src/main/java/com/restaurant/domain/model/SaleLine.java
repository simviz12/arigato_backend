package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Money;
import lombok.Value;

import java.util.UUID;

@Value
public class SaleLine {
    UUID saleId;
    UUID finalProductId;
    Integer quantitySold;
    Money unitPriceCentsAtSale;
    Money unitCostCentsAtSale;

    public SaleLine(UUID saleId, UUID finalProductId, Integer quantitySold, Money unitPriceCentsAtSale, Money unitCostCentsAtSale) {
        if (saleId == null) throw new IllegalArgumentException("Sale ID required");
        if (finalProductId == null) throw new IllegalArgumentException("Final product ID required");
        if (quantitySold == null || quantitySold <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPriceCentsAtSale == null || unitPriceCentsAtSale.getPesos().compareTo(java.math.BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price cannot be negative");
        if (unitCostCentsAtSale == null || unitCostCentsAtSale.getPesos().compareTo(java.math.BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Cost cannot be negative");

        this.saleId = saleId;
        this.finalProductId = finalProductId;
        this.quantitySold = quantitySold;
        this.unitPriceCentsAtSale = unitPriceCentsAtSale;
        this.unitCostCentsAtSale = unitCostCentsAtSale;
    }
}
