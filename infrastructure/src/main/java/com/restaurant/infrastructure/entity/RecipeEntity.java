package com.restaurant.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "recipes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeEntity {

    @Id
    private UUID id;

    @Column(name = "subproduct_id", nullable = false)
    private UUID subproductId;

    @Column(name = "ingredient_type", nullable = false)
    private String ingredientType; // "PRIMARY" or "SUBPRODUCT"

    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    @Column(name = "quantity_grams", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantityGrams;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
