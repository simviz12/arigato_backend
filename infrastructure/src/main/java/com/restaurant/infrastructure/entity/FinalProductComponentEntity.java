package com.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "final_product_components")
@Getter
@Setter
public class FinalProductComponentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_product_id", nullable = false)
    private FinalProductEntity finalProduct;

    @Column(name = "primary_product_id")
    private UUID primaryProductId;

    @Column(name = "subproduct_id")
    private UUID subproductId;

    @Column(name = "quantity_grams", nullable = false)
    private BigDecimal quantityGrams;
}
