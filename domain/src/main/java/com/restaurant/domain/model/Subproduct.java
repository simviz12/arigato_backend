package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Weight;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Subproduct {
    private final UUID id;
    private final String name;
    private final Weight totalYield;
    private final PreparationMode preparationMode;
    private Weight currentBatchStock;

    @Builder
    public Subproduct(UUID id, String name, Weight totalYield, PreparationMode preparationMode, Weight currentBatchStock) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Subproduct name cannot be empty");
        if (totalYield == null || totalYield.getGrams().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total yield must be positive");
        }
        if (preparationMode == null) throw new IllegalArgumentException("Preparation mode is required");

        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.totalYield = totalYield;
        this.preparationMode = preparationMode;
        
        if (preparationMode == PreparationMode.BATCH) {
            this.currentBatchStock = currentBatchStock != null ? currentBatchStock : Weight.ofGrams(0);
        } else {
            this.currentBatchStock = null; // ON_THE_FLY doesn't use batch stock
        }
    }

    public void deductBatchStock(Weight quantity) {
        if (this.preparationMode != PreparationMode.BATCH) {
            throw new IllegalStateException("Cannot deduct batch stock from an ON_THE_FLY subproduct");
        }
        if (this.currentBatchStock.getGrams().compareTo(quantity.getGrams()) < 0) {
            throw new com.restaurant.domain.exception.InsufficientBatchStockException(
                "Not enough batch stock for " + this.name + ". Needed: " + quantity.getGrams() + "g, Available: " + this.currentBatchStock.getGrams() + "g"
            );
        }
        this.currentBatchStock = Weight.ofGrams(this.currentBatchStock.getGrams().subtract(quantity.getGrams()));
    }
    
    public void addBatchStock(Weight quantity) {
        if (this.preparationMode != PreparationMode.BATCH) {
            throw new IllegalStateException("Cannot add batch stock to an ON_THE_FLY subproduct");
        }
        this.currentBatchStock = Weight.ofGrams(this.currentBatchStock.getGrams().add(quantity.getGrams()));
    }
}
