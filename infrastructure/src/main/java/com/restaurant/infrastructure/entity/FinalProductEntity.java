package com.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "final_products")
@Getter
@Setter
public class FinalProductEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "selling_price_cents", nullable = false)
    private Long sellingPriceCents;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    @OneToMany(mappedBy = "finalProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FinalProductComponentEntity> components = new ArrayList<>();
}
