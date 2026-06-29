package com.restaurant.infrastructure.entity;

import com.restaurant.domain.model.UnitOfMeasure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "primary_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryProductEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false)
    private UnitOfMeasure unitOfMeasure;

    @Column(name = "current_stock_grams", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal currentStockGrams = BigDecimal.ZERO;

    @Column(name = "current_stock_units", nullable = false)
    @Builder.Default
    private Integer currentStockUnits = 0;

    @Column(name = "minimum_stock_alert", precision = 12, scale = 2)
    private BigDecimal minimumStockAlert;

    @Column(name = "is_resale_item", nullable = false)
    @Builder.Default
    private boolean isResaleItem = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "preferred_distributor_id")
    private UUID preferredDistributorId;
}
