package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Weight;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class BatchPreparationLog {
    private final UUID id;
    private final UUID subproductId;
    private final Weight quantityPrepared;
    private final String preparedBy;
    private final LocalDateTime preparedAt;

    @Builder
    public BatchPreparationLog(UUID id, UUID subproductId, Weight quantityPrepared, String preparedBy, LocalDateTime preparedAt) {
        if (subproductId == null) throw new IllegalArgumentException("Subproduct ID is required");
        if (quantityPrepared == null || quantityPrepared.getGrams().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity prepared must be positive");
        }
        
        this.id = id != null ? id : UUID.randomUUID();
        this.subproductId = subproductId;
        this.quantityPrepared = quantityPrepared;
        this.preparedBy = preparedBy != null ? preparedBy : "SYSTEM";
        this.preparedAt = preparedAt != null ? preparedAt : LocalDateTime.now();
    }
}
