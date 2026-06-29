package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Purchase {
    private final UUID id;
    private final UUID primaryProductId;
    private final UUID distributorId;
    private final LocalDateTime purchaseDate;
    private final Weight quantity; // Serves as Grams or Units depending on Product
    private final Money totalPrice;

    @Builder
    public Purchase(UUID id, UUID primaryProductId, UUID distributorId, LocalDateTime purchaseDate,
                    Weight quantity, Money totalPrice) {
        if (primaryProductId == null) throw new IllegalArgumentException("Product ID is required");
        if (distributorId == null) throw new IllegalArgumentException("Distributor ID is required");
        if (quantity == null) throw new IllegalArgumentException("Quantity is required");
        if (totalPrice == null) throw new IllegalArgumentException("Total price is required");
        
        if (quantity.getGrams().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase quantity must be greater than zero");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.primaryProductId = primaryProductId;
        this.distributorId = distributorId;
        this.purchaseDate = purchaseDate != null ? purchaseDate : LocalDateTime.now();
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public Money getCostPerUnitOrGram() {
        return totalPrice.divideBy(quantity.getGrams());
    }
    
    // Legacy support to prevent massive refactoring errors in existing code
    public Weight getQuantityGrams() {
        return quantity;
    }
}
