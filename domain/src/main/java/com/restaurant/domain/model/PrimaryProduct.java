package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Weight;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PrimaryProduct implements Deductible {
    private final UUID id;
    private final String name;
    private final UnitOfMeasure unitOfMeasure;
    private Weight currentStock;
    private final Integer currentStockUnits;
    private final Weight minimumStockAlert;
    private final boolean isResaleItem;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final UUID preferredDistributorId;

    @Builder
    public PrimaryProduct(UUID id, String name, UnitOfMeasure unitOfMeasure, Weight currentStock,
                          Integer currentStockUnits, Weight minimumStockAlert, boolean isResaleItem,
                          LocalDateTime createdAt, LocalDateTime updatedAt, UUID preferredDistributorId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (unitOfMeasure == null) {
            throw new IllegalArgumentException("Unit of measure is required");
        }
        
        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.unitOfMeasure = unitOfMeasure;
        this.currentStock = currentStock != null ? currentStock : Weight.ofGrams(0);
        this.currentStockUnits = currentStockUnits != null ? currentStockUnits : 0;
        this.minimumStockAlert = minimumStockAlert;
        this.isResaleItem = isResaleItem;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.preferredDistributorId = preferredDistributorId;
    }

    public void addStock(Weight quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity to add cannot be null");
        }
        this.currentStock = this.currentStock.add(quantity);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeStock(Weight quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity to remove cannot be null");
        }
        if (this.currentStock.getGrams().compareTo(quantity.getGrams()) < 0) {
            throw new IllegalStateException("Not enough stock");
        }
        this.currentStock = this.currentStock.subtract(quantity);
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public void deduct(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deduction amount must be positive");
        }
        
        if (this.unitOfMeasure == UnitOfMeasure.GRAM) {
            removeStock(Weight.ofGrams(amount));
        } else {
            // It's UNIT based
            if (this.currentStock.getGrams().compareTo(amount) < 0) {
                throw new IllegalStateException("Not enough stock for unit-based product");
            }
            this.currentStock = Weight.ofGrams(this.currentStock.getGrams().subtract(amount));
            this.updatedAt = LocalDateTime.now();
        }
    }

    public boolean isStockLow() {
        if (minimumStockAlert == null) {
            return false;
        }
        return currentStock.getGrams().compareTo(minimumStockAlert.getGrams()) <= 0;
    }
}
