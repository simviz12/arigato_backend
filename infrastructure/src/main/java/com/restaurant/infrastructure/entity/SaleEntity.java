package com.restaurant.infrastructure.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales")
@Getter
@Setter
public class SaleEntity {

    @Id
    private UUID id;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "total_amount_cents", nullable = false)
    private Long totalAmountCents;

    @Column(name = "cash_amount_cents")
    private Long cashAmountCents;

    @Column(name = "nequi_amount_cents")
    private Long nequiAmountCents;

    @Column(name = "discount_cents")
    private Long discountCents;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleLineItemEntity> items = new ArrayList<>();
}
