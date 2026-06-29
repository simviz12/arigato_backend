package com.restaurant.domain.model;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class SubproductCostResult {
    BigDecimal costPerGramPesos;
    BigDecimal totalBatchCostPesos;

    public SubproductCostResult(BigDecimal costPerGramPesos, BigDecimal totalBatchCostPesos) {
        this.costPerGramPesos = costPerGramPesos != null ? costPerGramPesos : BigDecimal.ZERO;
        this.totalBatchCostPesos = totalBatchCostPesos != null ? totalBatchCostPesos : BigDecimal.ZERO;
    }
}
