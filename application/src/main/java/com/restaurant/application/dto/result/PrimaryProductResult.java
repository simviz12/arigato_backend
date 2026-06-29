package com.restaurant.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PrimaryProductResult {
    private UUID id;
    private String name;
    private String unitOfMeasure;
    private Double currentStockGrams;
    private Integer currentStockUnits;
    private Double minimumStockAlert;
    private boolean isResaleItem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
