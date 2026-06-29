package com.restaurant.infrastructure.repository;

import com.restaurant.domain.repository.ReportRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public class ReportRepositoryAdapter implements ReportRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BigDecimal getTotalIncome(LocalDateTime startDate, LocalDateTime endDate) {
        Long sumCents = entityManager.createQuery(
                "SELECT SUM(s.totalAmountCents) FROM SaleEntity s WHERE s.saleDate BETWEEN :start AND :end AND s.status != 'VOIDED'",
                Long.class)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getSingleResult();
        return sumCents != null ? new BigDecimal(sumCents).divide(new BigDecimal(100)) : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalCostOfGoodsSold(LocalDateTime startDate, LocalDateTime endDate) {
        Long sumCogsCents = entityManager.createQuery(
                "SELECT SUM(l.historicalUnitCostCents * l.quantity) FROM SaleLineItemEntity l " +
                "JOIN l.sale s WHERE s.saleDate BETWEEN :start AND :end AND s.status != 'VOIDED'",
                Long.class)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getSingleResult();
        return sumCogsCents != null ? new BigDecimal(sumCogsCents).divide(new BigDecimal(100)) : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalExpenses(LocalDateTime startDate, LocalDateTime endDate) {
        Long sumPurchasesCents = entityManager.createQuery(
                "SELECT SUM(p.totalPriceCents) FROM PurchaseEntity p WHERE p.purchaseDate BETWEEN :start AND :end",
                Long.class)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getSingleResult();
        return sumPurchasesCents != null ? new BigDecimal(sumPurchasesCents).divide(new BigDecimal(100)) : BigDecimal.ZERO;
    }
}
