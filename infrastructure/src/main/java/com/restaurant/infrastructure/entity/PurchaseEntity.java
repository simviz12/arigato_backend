package com.restaurant.infrastructure.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseEntity {
    @Id
    private UUID id;
    private UUID primaryProductId;
    private UUID distributorId;
    private LocalDateTime purchaseDate;
    private BigDecimal quantity;
    private Long totalPriceCents;
}
