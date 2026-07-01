package com.restaurant.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "distributor_offers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributorOfferEntity {

    @Id
    private UUID id;

    @Column(name = "distributor_id", nullable = false)
    private UUID distributorId;

    @Column(name = "primary_product_id", nullable = false)
    private UUID primaryProductId;

    @Column(name = "offered_quantity_grams", nullable = false, precision = 12, scale = 2)
    private BigDecimal offeredQuantityGrams;

    @Column(name = "offered_price_cents", nullable = false)
    private Long offeredPriceCents;

    @Column(name = "price_cents", nullable = false)
    private Long priceCents;

    @CreationTimestamp
    @Column(name = "valid_from", nullable = false, updatable = false)
    private LocalDateTime validFrom;
}
