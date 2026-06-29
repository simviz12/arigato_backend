package com.restaurant.infrastructure.repository;

import com.restaurant.domain.model.Sale;
import com.restaurant.domain.model.SaleLine;
import com.restaurant.domain.repository.SaleRepository;
import com.restaurant.infrastructure.entity.SaleEntity;
import com.restaurant.infrastructure.entity.SaleLineItemEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SaleRepositoryAdapter implements SaleRepository {

    private final SpringDataSaleRepository jpaRepository;

    @Override
    public void save(Sale sale, List<SaleLine> lines) {
        SaleEntity entity = new SaleEntity();
        entity.setId(sale.getId());
        entity.setSaleDate(sale.getSaleDate());
        entity.setTotalAmountCents(sale.getTotalAmount().getPesos().multiply(new java.math.BigDecimal("100")).longValue());
        entity.setPaymentMethod(sale.getPaymentMethod().name());
        entity.setCashAmountCents(sale.getCashAmountCents());
        entity.setNequiAmountCents(sale.getNequiAmountCents());
        entity.setDiscountCents(sale.getDiscount().getPesos().multiply(new java.math.BigDecimal("100")).longValue());

        List<SaleLineItemEntity> lineEntities = lines.stream().map(l -> {
            SaleLineItemEntity lineEntity = new SaleLineItemEntity();
            lineEntity.setId(java.util.UUID.randomUUID());
            lineEntity.setSale(entity);
            lineEntity.setFinalProductId(l.getFinalProductId());
            lineEntity.setQuantity(l.getQuantitySold());
            lineEntity.setUnitPriceCents(l.getUnitPriceCentsAtSale().getPesos().multiply(new java.math.BigDecimal("100")).longValue());
            lineEntity.setUnitCostCents(l.getUnitCostCentsAtSale().getPesos().multiply(new java.math.BigDecimal("100")).longValue());
            return lineEntity;
        }).collect(Collectors.toList());

        entity.setItems(lineEntities);
        jpaRepository.save(entity);
    }
}
