package com.restaurant.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DistributorOfferResult {
    private UUID id;
    private UUID distributorId;
    private UUID primaryProductId;
    private Double offeredQuantityGrams;
    private Double offeredPricePesos;
    private BigDecimal costPerGramPesos; // The computed ranking metric
    private LocalDateTime registeredAt;
}
