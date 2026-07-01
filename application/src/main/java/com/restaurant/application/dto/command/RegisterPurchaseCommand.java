package com.restaurant.application.dto.command;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RegisterPurchaseCommand {
    private UUID productId;
    private UUID distributorId;
    private BigDecimal quantity;
    private BigDecimal quantityGrams;
    private Integer quantityUnits;
    private BigDecimal totalPricePesos;

    public BigDecimal getQuantity() {
        if (quantity != null) return quantity;
        if (quantityGrams != null) return quantityGrams;
        if (quantityUnits != null) return BigDecimal.valueOf(quantityUnits);
        return null;
    }
}
