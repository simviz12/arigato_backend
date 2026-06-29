package com.restaurant.infrastructure.entity;

import com.restaurant.domain.model.PreparationMode;
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
@Table(name = "subproducts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubproductEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "total_yield_grams", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalYieldGrams;

    @Enumerated(EnumType.STRING)
    @Column(name = "preparation_mode", nullable = false)
    private PreparationMode preparationMode;

    @Column(name = "current_batch_stock_grams", precision = 12, scale = 2)
    private BigDecimal currentBatchStockGrams;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
