package com.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sale_items")
@Getter
@Setter
public class SaleLineItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleEntity sale;

    @Column(name = "final_product_id", nullable = false)
    private UUID finalProductId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price_cents", nullable = false)
    private Long unitPriceCents;

    @Column(name = "unit_cost_cents", nullable = false)
    private Long unitCostCents;
}
