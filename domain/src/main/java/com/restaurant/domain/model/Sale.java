package com.restaurant.domain.model;

import com.restaurant.domain.model.vo.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Sale {
    private final UUID id;
    private final String cashierId;
    private final LocalDateTime saleDate;
    private final PaymentMethod paymentMethod;
    private final Money totalAmount;
    private final Money discount;
    private final Long cashAmountCents;
    private final Long nequiAmountCents;
    private SaleStatus status;

    @Builder
    public Sale(UUID id, String cashierId, LocalDateTime saleDate, PaymentMethod paymentMethod, Money totalAmount, Money discount, Long cashAmountCents, Long nequiAmountCents, SaleStatus status) {
        if (cashierId == null || cashierId.isBlank()) throw new IllegalArgumentException("Cashier ID required");
        if (paymentMethod == null) throw new IllegalArgumentException("Payment method required");
        if (totalAmount == null || totalAmount.getPesos().compareTo(java.math.BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Total amount cannot be negative");

        this.id = id != null ? id : UUID.randomUUID();
        this.cashierId = cashierId;
        this.saleDate = saleDate != null ? saleDate : LocalDateTime.now();
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.discount = discount != null ? discount : Money.ofPesos(0);
        this.cashAmountCents = cashAmountCents != null ? cashAmountCents : 0L;
        this.nequiAmountCents = nequiAmountCents != null ? nequiAmountCents : 0L;
        this.status = status != null ? status : SaleStatus.COMPLETED;
    }

    public void voidSale() {
        this.status = SaleStatus.VOIDED;
    }
}
