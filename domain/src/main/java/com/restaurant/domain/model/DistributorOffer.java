package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DistributorOffer {
    private final UUID id;
    private final UUID distributorId;
    private final UUID primaryProductId;
    private final Weight offeredQuantity;
    private final Money offeredPrice;
    private final LocalDateTime registeredAt;

    @Builder
    public DistributorOffer(UUID id, UUID distributorId, UUID primaryProductId, Weight offeredQuantity, Money offeredPrice, LocalDateTime registeredAt) {
        if (distributorId == null) throw new IllegalArgumentException("Distributor ID is required");
        if (primaryProductId == null) throw new IllegalArgumentException("Primary Product ID is required");
        if (offeredQuantity == null || offeredQuantity.getGrams().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Offered quantity must be strictly positive");
        }
        if (offeredPrice == null || offeredPrice.getCents() < 0) {
            throw new IllegalArgumentException("Offered price cannot be negative");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.distributorId = distributorId;
        this.primaryProductId = primaryProductId;
        this.offeredQuantity = offeredQuantity;
        this.offeredPrice = offeredPrice;
        this.registeredAt = registeredAt != null ? registeredAt : LocalDateTime.now();
    }
}
