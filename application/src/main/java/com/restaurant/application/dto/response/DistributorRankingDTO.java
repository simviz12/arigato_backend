package com.restaurant.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributorRankingDTO {
    private UUID distributorId;
    private String distributorName;
    private BigDecimal averagePricePerGram;
    private long totalPurchases;
}
